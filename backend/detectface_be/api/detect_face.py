import os
import cv2
import base64
import aiofiles
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.core.files.storage import FileSystemStorage
from face_manipulation.face_manipulation import FaceMainipulation
from config.s3_storage import S3Storage

# Singleton pattern to ensure only one instance of FaceMainipulation exists
class SingletonFaceManipulation:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(SingletonFaceManipulation, cls).__new__(cls)
            cls._instance.model = FaceMainipulation()
            cls._instance.model.load_model()
        return cls._instance

# Set up
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

@csrf_exempt
def manipulate_face(request):
    if request.method == 'POST':
        try:
            # Save uploaded file to disk
            file = request.FILES['file']
            fs = FileSystemStorage(location=debug_path)
            image_path = fs.save(file.name, file)
            image_path = os.path.join(debug_path, image_path)

            # Process the image
            colormap, visimage = process_image_sync(image_path)

            if colormap is None:
                return JsonResponse({"status": False})

            # Save processed images to disk
            colormap_src = os.path.join(debug_path, "colormap_" + file.name)
            visimage_src = os.path.join(debug_path, "visimage_" + file.name)
            cv2.imwrite(colormap_src, colormap)
            cv2.imwrite(visimage_src, visimage)

            # Convert images to base64
            image_input = image_to_base64(image_path)
            colormap_base64 = image_to_base64(colormap_src)
            visimage_base64 = image_to_base64(visimage_src)

            # Return JSON response with base64-encoded images
            return JsonResponse({
                "status": True,
                "imageInput": image_input,
                "colormap": colormap_base64,
                "visimage": visimage_base64
            })
        except Exception as e:
            return JsonResponse({"status": False, "error": str(e)}, status=500)
    else:
        return JsonResponse({"status": False, "error": "Invalid request method."}, status=405)
