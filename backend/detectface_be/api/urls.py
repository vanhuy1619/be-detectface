from django.urls import path
from . import detect_face  # Import the view function from detect_face

urlpatterns = [
    path('manipulate_face', detect_face.manipulate_face, name='manipulate_face'),
]
