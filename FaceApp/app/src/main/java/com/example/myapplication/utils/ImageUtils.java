package com.example.myapplication.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageUtils {
    // Giải mã chuỗi base64 thành mảng byte
    private static byte[] decodeBase64(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    // Chuyển đổi mảng byte thành Bitmap
    public static Bitmap convertBase64ToBitmap(String base64String) {
        byte[] decodedString = decodeBase64(base64String);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
