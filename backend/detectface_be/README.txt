# Hướng dẫn sử dụng dự án Django

## Cài đặt các gói cần thiết
** Copy file saved_model.bin, saved_model.xml, labelmap.pbtxt vào folder saved_model
Chạy lệnh sau để cài đặt tất cả các gói từ file `requirements.txt`:

```bash
pip install -r requirements.txt

Chạy máy chủ phát triển
Sau khi cài đặt các gói, bạn có thể khởi động máy chủ phát triển bằng lệnh:

```bash
Sao chép mã sau để start dự án
python manage.py runserver


Kiểm tra API bằng Postman
Bạn có thể kiểm tra API bằng cách gửi yêu cầu đến địa chỉ http://localhost:8000/manipulate_face với Postman.

Gửi yêu cầu bằng curl
Sử dụng lệnh curl sau để gửi yêu cầu đến API với file hình ảnh:

```bash
Sao chép mã
curl --location 'localhost:8000/manipulate_face' \
--form 'file=@"/C:/Users/admin/Downloads/crop-20240725T035847Z-001/crop/flickr_0500.png"'

Sử dụng kiểu form-data
Key: file
Value: chọn một ảnh bất kỳ
==> Send

###. Cấu trúc thư mục

Ứng dụng của bạn cần có cấu trúc thư mục như sau:

```
C:.
|   .env
|   db.sqlite3
|   manage.py
|   README.txt
|   requirements.txt
|   
+---api
|   |   detect_face.py
|   |   urls.py
|   |   __init__.py
|   |   
|   \---__pycache__
|           detect_face.cpython-312.pyc
|           urls.cpython-312.pyc
|           __init__.cpython-312.pyc
|
+---config
|   |   s3_storage.py
|   |   __init__.py
|   |
|   \---__pycache__
|           s3_storage.cpython-312.pyc
|           __init__.cpython-312.pyc
|
+---debug
|       colormap_flickr_0500.png
|       flickr_0500.png
|       flickr_0500_Anj9XGa.png
|       flickr_0500_FNeiurY.png
|       flickr_0500_kYCWYbR.png
|       flickr_0500_q70xKno.png
|       flickr_0500_SCIvAJi.png
|       flickr_0500_ZZxxX8F.png
|       visimage_flickr_0500.png
|
+---detectface_be
|   |   asgi.py
|   |   settings.py
|   |   urls.py
|   |   wsgi.py
|   |   __init__.py
|   |
|   \---__pycache__
|           settings.cpython-312.pyc
|           urls.cpython-312.pyc
|           wsgi.cpython-312.pyc
|           __init__.cpython-312.pyc
|
+---face_manipulation
|   |   face_manipulation.py
|   |   tf2net_openvino.py
|   |   __init__.py
|   |
|   +---debug
|   \---__pycache__
|           face_manipulation.cpython-312.pyc
|           tf2net_openvino.cpython-312.pyc
|           __init__.cpython-312.pyc
|
\---saved_model
        labelmap.pbtxt
        saved_model.bin
        saved_model.xml
```