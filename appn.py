import os
import cv2
import uvicorn
from PIL import Image
from fastapi import FastAPI
from pydantic import BaseModel
from connections import S3Storage
from face_manipulation import FaceMainipulation


def image_to_base64(image_path):
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    return encoded_string


def base64_to_image(base64_string, output_path):
    try:
        with open(output_path, "wb") as fh:
            fh.write(base64.b64decode(base64_string))
            return True
    except:
        pass
    


# create debug folder
debug_path = "debug"
os.makedirs(debug_path, exist_ok=True)


# init connection and model
app = FastAPI()
face_manipulation = FaceMainipulation()
face_manipulation.load_model()


# create API
class Image(BaseModel):
    image_path: str
    image_ba64: str
    
@app.post("/manipulate_face")
def manipulate_face(request: Image):
    
    image_path = request.image_path
    image_ba64 = request.image_ba64
    image_save = os.path.join(debug_path, os.path.basename(image_path))
    is_downloaded = base64_to_image(base64_string=image_ba64, output_path=image_save)
    if is_downloaded is not True: return {"status": False}

    output = face_manipulation.segment_modified(image_save)
    if output is None: return {"status": False}
    else:
        colormap, visimage = output
        colormap_path = os.path.join(debug_path, "colormap_" + os.path.basename(image_path))
        cv2.imwrite(colormap_path, colormap)
        visimage_path = os.path.join(debug_path, "visimage_" + os.path.basename(image_path))
        cv2.imwrite(visimage_path, visimage)
        return {"status": True, "colormap": image_to_base64(colormap_path), "visimage": image_to_base64(visimage_path)}


if __name__ == '__main__': uvicorn.run(app)
