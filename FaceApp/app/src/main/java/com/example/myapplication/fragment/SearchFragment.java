package com.example.myapplication.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.example.myapplication.R;
import com.example.myapplication.adapter.FishAdapter;
import com.example.myapplication.config.S3Uploader;
import com.example.myapplication.model.DataItem;
import com.example.myapplication.model.ImageDetect;
import com.example.myapplication.utils.ImageUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import org.json.JSONObject;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchFragment extends Fragment {
    private RecyclerView rcvFish;
    private FishAdapter fishAdapter;
    private SearchView searchView;
    private ImageButton buttonAddPhoto;
    private ImageButton buttonSelectPhoto;
    private View view;
    private FragmentActivity activity;
    private Context context;
    private String currentPhotoPath;
    private S3Uploader s3Uploader;
    private ProgressBar progressBar;
    private FrameLayout processOverlay;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        // Khởi tạo S3Uploader với tên bucket S3
        s3Uploader = new S3Uploader(activity);

        if (activity == null)
            Log.d("SearchFragment", "onCreate: activity is null");
    }

    private void cropImage(Uri imageUri) {
        Intent cropImageIntent = CropImage.activity(imageUri).getIntent(requireContext());
        cropImageResultLauncher.launch(cropImageIntent);
    }

    //xử lý ket qua tu viec cat anh
    final ActivityResultLauncher<Intent> cropImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
                            Uri imageUri = cropResult.getUri();
                            if (imageUri != null) {
                                try {
                                    File croppedImageFile = new File(imageUri.getPath());

                                    // Tải ảnh đã cắt lên S3
//                                    uploadImageToS3(croppedImageFile);
                                    new SearchFragment.APICaller(croppedImageFile).execute();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else
                                Log.d("SearchFragment", "cropImageResultLauncher: imageUri is null");
                        } else
                            Log.d("SearchFragment", "cropImageResultLauncher: data is null");
                    }
                }
            });

    // thêm ảnh vào thư viện sau khi chụp
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        view.getContext().sendBroadcast(mediaScanIntent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            context = view.getContext();
            rcvFish = view.findViewById(R.id.rcv_fish);
            searchView = view.findViewById(R.id.searchView);
            buttonAddPhoto = view.findViewById(R.id.btn_add_photo);
            buttonSelectPhoto = view.findViewById(R.id.btn_select_photo);
            progressBar = view.findViewById(R.id.progressBar);
            processOverlay = view.findViewById(R.id.progress_overlay);

        } else
            Log.d("SearchFragment", "onCreateView: view is null");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rcvFish.setLayoutManager(linearLayoutManager);

        fishAdapter = new FishAdapter(this.context , DataItem.allFishes);
        rcvFish.setAdapter(fishAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
        rcvFish.addItemDecoration(itemDecoration);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fishAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fishAdapter.getFilter().filter(newText);
                return false;
            }
        });

        //chụp ảnh bằng camera
        cameraPhoto();

        //chọn aảnh từ thư viện
        selectPhtoFromGallery();

        return view;
    }

    private void cameraPhoto() {
        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View view) {
                Log.d("SearchFragment", "buttonAddPhoto is clicked");
                currentPhotoPath = "";

                //chứa quyền cần thiết
                String[] requiredPermissions = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                boolean grantedAllPermissions = true; //quyền đã được cấp
                for (String permission : requiredPermissions) {
                    if (ContextCompat.checkSelfPermission(context, permission)
                            == PackageManager.PERMISSION_DENIED)
                        grantedAllPermissions = false;
                } //nếu quyền nào chưa được cấp ==> false

                if (!grantedAllPermissions) {
                    ActivityCompat.requestPermissions(
                            activity,
                            requiredPermissions,
                            100
                    );
                } // nếu chưa chưa đc cấu quyn thì yêu cầu từ người dùng

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //intent gọi ứng dụng camera
                if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) { //ứng dụng nào có th xử lý yêu cầu chp ảnh
                    File photoFile = null;
                    try {
                        photoFile = createImageFile(); //tạo file lưu ảnh
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(
                                view.getContext(),
                                "com.example.android.fileprovider",
                                photoFile
                        );
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        takePictureActivityResultLauncher.launch(takePictureIntent);
                    }
                }
            }

            //dăng ký lắng nghe kết quả từ việc chụp ảnh
            final ActivityResultLauncher<Intent> takePictureActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) { //kết quả trả về từ camera
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                File capturedImage = new File(currentPhotoPath);
                                if (capturedImage.exists()) {
                                    Uri contentUri = Uri.fromFile(capturedImage); //chuyển đổi đường dẫn file thành uri
                                    cropImage(contentUri);
                                    galleryAddPic(); //thêm ảnh vào thư viện
                                }
                            }
                        }
                    });
        });
    }

    private void selectPhtoFromGallery() {
        buttonSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                selectPictureActivityResultLauncher.launch(chooserIntent);
            }

            final ActivityResultLauncher<Intent> selectPictureActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                // There are no request codes
                                Intent data = result.getData();
                                Uri contentUri = Objects.requireNonNull(data).getData();
                                if (contentUri != null)
                                    cropImage(contentUri);
                            }
                        }
                    });
        });
    }

    private void showResult(Bitmap inputImage, Bitmap maskImage, Bitmap heatmapImage) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment showResultFragment = new ShowResultFragment(inputImage, maskImage, heatmapImage);
        fragmentManager.beginTransaction()
                .replace(R.id.flContent, showResultFragment)
                .addToBackStack(null)
                .commit();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = view.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + File.separator + "FindYourDriedFish"
        );
        boolean storageDirExist = storageDir.exists();
        if (!storageDirExist) {
            Log.d("SearchFragment", "createImageFile: storageDir not exists");
            storageDirExist = storageDir.mkdirs();
        }

        if (storageDirExist) {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = image.getAbsolutePath();
            return image;
        }

        return null;
    }

    private MediaType getMediaTypeForFile(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return MediaType.parse("image/jpeg");
        } else if (fileName.endsWith(".png")) {
            return MediaType.parse("image/png");
        } else if (fileName.endsWith(".gif")) {
            return MediaType.parse("image/gif");
        } else {
            // Mặc định cho tệp hình ảnh không xác định
            return MediaType.parse("application/octet-stream");
        }
    }

//    private void uploadImageToS3(File imageFile) {
//        String s3Key = imageFile.getName();
//        s3Uploader.uploadImage(imageFile, s3Key, new S3Uploader.UploadCallback() {
//            @Override
//            public void onUploadSuccess(String imageUrl) {
//                Log.d("S3", "Image uploaded successfully: " + imageUrl);
//                new SearchFragment.APICaller(imageUrl).execute();
//            }
//
//            @Override
//            public void onUploadFailed(Exception ex) {
//                Log.e("S3", "Image upload failed", ex);
//            }
//        });
//    }

    private class APICaller extends AsyncTask<Void, Void, Pair<String, ImageDetect>> {
        private File fileImage;
        private String urlDetectApp = "http://10.0.2.2:8000/manipulate_face";

        APICaller(File fileImage) {
            this.fileImage = fileImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE); // Hiển thị màn hình loading
                processOverlay.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Pair<String, ImageDetect> doInBackground(Void... voids) {
            Log.d("APICaller", "Call API: "+urlDetectApp);
            Log.d("APICaller", "imageInput: "+fileImage.getAbsolutePath());

            // Tạo một ConnectionPool với tối đa 5 kết nối và thời gian chờ 5 phút
            ConnectionPool connectionPool = new ConnectionPool(60, 5, TimeUnit.MINUTES);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS) // Thời gian chờ kết nối
                    .writeTimeout(300, TimeUnit.SECONDS)    // Thời gian chờ ghi dữ liệu
                    .readTimeout(300, TimeUnit.SECONDS) // Thời gian chờ đọc dữ liệu
                    .connectionPool(connectionPool)
                    .build();
            Pair<String, ImageDetect> detectResult = null;

            try {
                // Tạo File từ URL của ảnh
//                File file = new File(imageUrl); // Đảm bảo rằng imageUrl là đường dẫn đầy đủ đến tệp ảnh trên thiết bị của bạn

                // Xác định MediaType dựa trên phần mở rộng của tệp
                MediaType mediaType = getMediaTypeForFile(fileImage);

                // Tạo RequestBody cho multipart/form-data
                RequestBody fileBody = RequestBody.create(fileImage, mediaType);

                RequestBody multipartBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileImage.getName(), fileBody)
                        .build();

                Request request = new Request.Builder()
                        .url(urlDetectApp)
                        .post(multipartBody)
                        .build();

                Log.d("APICaller", "request: "+request);

                Response response = client.newCall(request).execute();
                Log.d("APICaller", "response: " + response);

                if (!response.isSuccessful()) {
                    Log.e("APICaller", "Unexpected code: "+response);
                    throw new IOException("Unexpected code " + response);
                }

                // Parse the response to get image paths
                JSONObject responseJson = new JSONObject(response.body().string());
                String imageInput = responseJson.getString("imageInput");
                String colormap = responseJson.getString("colormap");
                String visimage = responseJson.getString("visimage");

                ImageDetect imageDetect = new ImageDetect(colormap, visimage);
                detectResult = new Pair<>(imageInput, imageDetect);

            } catch (Exception e) {
                Log.e("APICaller", "Exception: "+e);
                e.printStackTrace();
            }

            return detectResult;
        }

        @Override
        protected void onPostExecute(Pair<String, ImageDetect> result) {
            super.onPostExecute(result);
            if(progressBar != null) {
                progressBar.setVisibility(View.GONE);
                processOverlay.setVisibility(View.GONE);
            }
            if (result != null) {
                String colormap = result.second.getColormap();
                String visimage = result.second.getVisimage();
                new LoadImagesTask(result.first, colormap, visimage).execute();
            } else {
                Log.e("APICaller", "Post API Failed");
                SweetAlertDialog errorAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("ERROR")
                        .setContentText("The photo you uploaded could not be processed.");

                errorAlertDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(errorAlertDialog.isShowing()) {
                            errorAlertDialog.dismiss();
                        }
                    }
                }, 10000); //hiện trong 5s
            }
        }
    }

    private class LoadImagesTask extends AsyncTask<Void, Void, Pair<String, String>> {
        private String imageInput;
        private String colormap;
        private String visimage;

        LoadImagesTask(String imageInput, String maskImagePath, String heatmapImagePath) {
            this.imageInput = imageInput;
            this.colormap = maskImagePath;
            this.visimage = heatmapImagePath;
        }

        @Override
        protected Pair<String, String> doInBackground(Void... voids) {
            return new Pair<>(colormap, visimage);
        }

        @Override
        protected void onPostExecute(Pair<String, String> result) {
            if (result != null) {
                showResult(
                        ImageUtils.convertBase64ToBitmap(imageInput),
                        ImageUtils.convertBase64ToBitmap(result.first),
                        ImageUtils.convertBase64ToBitmap(result.second)
                );
            };
        }
    }

    public SearchFragment() {
        super(R.layout.search_activity);
    }
}
