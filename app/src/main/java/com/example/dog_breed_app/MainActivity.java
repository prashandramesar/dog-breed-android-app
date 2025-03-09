package com.example.dog_breed_app;
import okhttp3.MediaType;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.dog_breed_app.ApiService;
import com.example.dog_breed_app.ApiClient;
import com.example.dog_breed_app.PredictionResponse;

import java.io.InputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_GALLERY_PERMISSION = 102;
    private static final int REQUEST_IMAGE_CAPTURE = 103;
    private static final int REQUEST_PICK_IMAGE = 104;

    private ImageView imagePreview;
    private Button cameraButton, galleryButton, classifyButton;
    private ProgressBar progressBar;
    private TextView resultTextView;

    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        classifyButton = findViewById(R.id.classifyButton);
        progressBar = findViewById(R.id.progressBar);
        resultTextView = findViewById(R.id.resultTextView);

        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Set button click listeners
        cameraButton.setOnClickListener(v -> requestCameraPermission());
        galleryButton.setOnClickListener(v -> requestGalleryPermission());
        classifyButton.setOnClickListener(v -> classifyImage());
    }

    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private void requestCameraPermission() {
        // For Android 10 and below
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
            String[] perms = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(this, perms)) {
                openCamera();
            } else {
                EasyPermissions.requestPermissions(this, "We need camera permission to take pictures",
                        REQUEST_CAMERA_PERMISSION, perms);
            }
        } else {
            // For Android 11+
            String[] perms = {android.Manifest.permission.CAMERA};
            if (EasyPermissions.hasPermissions(this, perms)) {
                openCamera();
            } else {
                EasyPermissions.requestPermissions(this, "We need camera permission to take pictures",
                        REQUEST_CAMERA_PERMISSION, perms);
            }
        }
    }

    @AfterPermissionGranted(REQUEST_GALLERY_PERMISSION)
    private void requestGalleryPermission() {
        // For Android 10 and below
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
            String[] perms = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(this, perms)) {
                openGallery();
            } else {
                EasyPermissions.requestPermissions(this, "We need storage permission to access your gallery",
                        REQUEST_GALLERY_PERMISSION, perms);
            }
        } else {
            // For Android 11+, we need READ_MEDIA_IMAGES
            String[] perms = {android.Manifest.permission.READ_MEDIA_IMAGES};
            if (EasyPermissions.hasPermissions(this, perms)) {
                openGallery();
            } else {
                EasyPermissions.requestPermissions(this, "We need permission to access your photos",
                        REQUEST_GALLERY_PERMISSION, perms);
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.example.dog_breed_app.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createFileFromUri(Uri uri) {
        try {
            // Create a file to copy the image content
            File destination = new File(getCacheDir(), "temp_image.jpg");

            // Create input and output streams
            InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destination);

            // Copy the file content
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return destination;
        } catch (Exception e) {
            Log.e(TAG, "Error creating file from Uri", e);
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                loadImageIntoPreview(currentPhotoUri);
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                currentPhotoUri = selectedImageUri;
                loadImageIntoPreview(selectedImageUri);
            }
        }
    }

    private void loadImageIntoPreview(Uri imageUri) {
        // Load image into preview
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imagePreview);

        // Enable classify button
        classifyButton.setEnabled(true);

        // Clear previous results
        resultTextView.setText("");
    }

    private void classifyImage() {
        if (currentPhotoUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        resultTextView.setText("Analyzing...");
        classifyButton.setEnabled(false);

        try {
            // Create a file from the URI
            File imageFile = createFileFromUri(currentPhotoUri);

            if (imageFile == null || !imageFile.exists()) {
                Toast.makeText(this, "Error: Cannot access the image file", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                classifyButton.setEnabled(true);
                return;
            }

            // Create multipart request
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            // Make API call
            apiService.predictDogBreed(body).enqueue(new Callback<PredictionResponse>() {
                @Override
                public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    classifyButton.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        displayResults(response.body().getPredictions());
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            resultTextView.setText("Error: " + errorBody);
                        } catch (IOException e) {
                            resultTextView.setText("Error: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<PredictionResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    classifyButton.setEnabled(true);
                    resultTextView.setText("Error: " + t.getMessage());
                    Log.e(TAG, "API call failed", t);
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            classifyButton.setEnabled(true);
            resultTextView.setText("Error: " + e.getMessage());
            Log.e(TAG, "Error processing image", e);
        }
    }

    private void displayResults(List<PredictionResponse.Prediction> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            resultTextView.setText("No predictions received");
            return;
        }

        StringBuilder resultText = new StringBuilder("Results:\n\n");
        for (PredictionResponse.Prediction prediction : predictions) {
            String breedName = prediction.getBreed().replace("_", " ");
            breedName = breedName.substring(0, 1).toUpperCase() + breedName.substring(1);

            resultText.append(breedName)
                    .append(": ")
                    .append(String.format("%.1f%%", prediction.getConfidence() * 100))
                    .append("\n");
        }

        resultTextView.setText(resultText.toString());
    }

    private String getPath(Uri uri) {
        // This is a simplified version. In a real app, you might need more complex logic
        // to handle different URI types and Android versions.
        return uri.getPath();
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        // Create a temporary file
        File outputFile = File.createTempFile("temp_", ".jpg", getCacheDir());

        // Copy content from URI to the file (this part would be more complex in a real app)
        // For this example, we'll assume it works
        return outputFile;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permissions granted: " + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permissions denied: " + perms.size());
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}