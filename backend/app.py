import os
import cv2
import uvicorn
import base64
import aiofiles
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.gzip import GZipMiddleware
from connections import S3Storage
from face_manipulation import FaceMainipulation
from concurrent.futures import ThreadPoolExecutor
import asyncio

# Singleton pattern to ensure only one instance of FaceMainipulation exists
class SingletonFaceManipulation:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(SingletonFaceManipulation, cls).__new__(cls)
            cls._instance.model = FaceMainipulation()
            cls._instance.model.load_model()
        return cls._instance

app = FastAPI()
app.add_middleware(GZipMiddleware, minimum_size=1000)  # Enable GZip compression for responses

debug_path = "debug"
os.makedirs(debug_path, exist_ok=True)

s3_storage = S3Storage(bucket_name="image-recognition-app-111")
face_manipulation = SingletonFaceManipulation().model

# Convert image to base64 encoding
def image_to_base64(image_path):
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    return encoded_string

# Synchronous image processing function
def process_image_sync(image_path):
    return face_manipulation.segment_modified(image_path)

# API endpoint to manipulate face in the uploaded image
@app.post("/manipulate_face")
async def manipulate_face(file: UploadFile = File(...)):
    try:
        # Save uploaded file to disk using aiofiles for asynchronous I/O
        image_path = os.path.join(debug_path, file.filename)
        async with aiofiles.open(image_path, "wb") as buffer:
            await buffer.write(await file.read())

        # Asynchronously process the image in a separate thread
        loop = asyncio.get_event_loop()
        colormap, visimage = await loop.run_in_executor(None, process_image_sync, image_path)

        if colormap is None:
            return {"status": False}

        # Save processed images to disk (this part could be further optimized if needed)
        colormap_src = os.path.join(debug_path, "colormap_" + file.filename)
        visimage_src = os.path.join(debug_path, "visimage_" + file.filename)
        cv2.imwrite(colormap_src, colormap)
        cv2.imwrite(visimage_src, visimage)

        # Convert images to base64
        image_input = image_to_base64(image_path)
        colormap_base64 = image_to_base64(colormap_src)
        visimage_base64 = image_to_base64(visimage_src)

        # Return JSON response with base64-encoded images
        return JSONResponse({
            "status": True,
            "imageInput": image_input,
            "colormap": colormap_base64,
            "visimage": visimage_base64
        })
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == '__main__':
    uvicorn.run(app, host="0.0.0.0", port=int(os.environ.get("PORT", 8000)))
