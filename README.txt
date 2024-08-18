
# Hướng dẫn chạy ứng dụng

## 1. Cài đặt môi trường

1. Cài đặt Python (phiên bản 3.x) từ [Python Official Website](https://www.python.org/).
2. Tạo môi trường ảo để quản lý các package:
   ```bash
   python -m venv venv
   ```
3. Kích hoạt môi trường ảo:

   - **Windows:**
     ```bash
     .\venv\Scripts\activate
     ```
   - **MacOS/Linux:**
     ```bash
     source venv/bin/activate
     ```

4. Cài đặt các dependencies từ file `requirements.txt` (nếu có):

   ```bash
   pip install -r requirements.txt
   ```

   Nếu bạn chưa có file `requirements.txt`, bạn có thể tạo file này bằng cách:
   
   ```bash
   pip freeze > requirements.txt
   ```

5. Cài đặt các thư viện cần thiết theo yêu cầu của ứng dụng:

   ```bash
   pip install fastapi uvicorn opencv-python boto3 aiofiles python-dotenv
   ```

   xem trong các file .py và cài đătk đủ thư viện (nếu chưa có)

## 2. Thiết lập biến môi trường

1. Tạo file `.env` trong thư mục gốc của dự án.
2. Thêm các biến môi trường cần thiết (ví dụ: thông tin AWS credentials) vào file `.env`. Ví dụ:

   ```
   AWS_ACCESS_KEY_ID=your_access_key
   AWS_SECRET_ACCESS_KEY=your_secret_key
   ```

## 3. Cấu trúc thư mục

Ứng dụng của bạn cần có cấu trúc thư mục như sau:

```
/project-root
│
├── app.py
├── connections.py
├── face_manipulation.py
├── tf2net_openvino.py  # (nếu có)
├── requirements.txt
└── .env
```

## 4. Chạy ứng dụng

Để chạy ứng dụng FastAPI, bạn có thể sử dụng `uvicorn` như sau:

```bash
python app.py
```

- **`app:app`** - Tham số đầu tiên là tên file Python (không bao gồm phần mở rộng `.py`), và tham số thứ hai là tên của đối tượng FastAPI trong file `app.py`.
- **`--reload`** - Tham số này giúp tự động tải lại ứng dụng khi có thay đổi trong mã nguồn.

Sau khi chạy lệnh này, ứng dụng sẽ được khởi chạy và bạn có thể truy cập vào địa chỉ `http://127.0.0.1:8000` để kiểm tra.
localhost:8000/manipulate_face

curl tham khảo test bằng postman:
curl --location 'localhost:8000/manipulate_face' \
--form 'file=@"/C:/Users/admin/Downloads/crop-20240725T035847Z-001/crop/flickr_0501.png"'

Sử dụng kiểu form-data
Key: file
Value: chọn một ảnh bất kỳ
==> Send

## 5. Ghi chú thêm

- **Upload file**: Đảm bảo rằng bạn đã cấu hình đúng S3 bucket trong `connections.py`.
- **Xử lý hình ảnh**: Module `face_manipulation.py` sẽ sử dụng OpenCV để xử lý ảnh và module `tf2net_openvino.py` để phân đoạn đối tượng.
