package com.example.myapplication.config;

import android.content.Context;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import java.io.File;

public class S3Uploader {
    private final AmazonS3 s3Client;
    private final TransferUtility transferUtility;
    private final String bucketName = "image-recognition-app-111";
    private final String accessKey = "AKIA45TM4LYNW54TQS7T";
    private final String secretKey = "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";

    public S3Uploader(Context context) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = new AmazonS3Client(awsCredentials);
//        this.s3Client.setRegion(Region.getRegion(Regions.US_EAST_2)); // Replace with your region

        this.transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build();
    }

    public void uploadImage(File imageFile, String s3Key, UploadCallback callback) {
        transferUtility.upload(
                bucketName,
                s3Key,
                imageFile
        ).setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // Upload successful, create URL to access image
                    String imageUrl = s3Client.getUrl(bucketName, s3Key).toString();
                    callback.onUploadSuccess(imageUrl);
                } else if (state == TransferState.FAILED) {
                    callback.onUploadFailed(new Exception("Upload failed"));
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                // Optionally update UI with upload progress
            }

            @Override
            public void onError(int id, Exception ex) {
                callback.onUploadFailed(ex);
            }
        });
    }

    public interface UploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailed(Exception ex);
    }
}

