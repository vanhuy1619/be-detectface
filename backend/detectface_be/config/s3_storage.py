import os
import boto3
from dotenv import load_dotenv


class S3Storage:

    load_dotenv()
    aws_access_key_id = os.environ["AWS_ACCESS_KEY_ID"]
    aws_secret_access_key = os.environ["AWS_SECRET_ACCESS_KEY"]
    s3_client = boto3.client('s3', aws_access_key_id=aws_access_key_id, aws_secret_access_key=aws_secret_access_key)

    def __init__(self, bucket_name) -> None:
        self.bucket_name = bucket_name

    def upload_file(self, src_path, des_path):
        try:
            if os.path.exists(src_path):
                self.s3_client.upload_file(src_path, self.bucket_name, des_path)
                return des_path
        except Exception as e:
            print(f"An error occurred: {e}")
            pass

    def download_file(self, src_path, des_path):
        try:
            self.s3_client.download_file(self.bucket_name, src_path, des_path)
            if os.path.exists(des_path):
                return True
        except Exception as e:
            print(f"An error occurred: {e}")
            pass
