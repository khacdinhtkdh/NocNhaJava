package com.example.nocnha.controller;

import static com.example.nocnha.Constants.EXTRA_URL;
import static com.example.nocnha.Constants.STORAGE_PERMISSION_CODE;
import static com.example.nocnha.Constants.USERS;
import static com.example.nocnha.Constants.USER_IMAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nocnha.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateInfoActivity extends AppCompatActivity {
    private final String TAG = "KD_UPDATE_IF";
    private String userImgUrl;
    private StorageReference firebaseReference;
    private DatabaseReference databaseReference;
    private String coverCheck;
    ProgressBar updateProgressBar;
    ImageView imgShowProfile;
    private Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        Intent intent = getIntent();
        userImgUrl = intent.getStringExtra(EXTRA_URL);

        imgShowProfile = findViewById(R.id.imgShowProfile);
        updateProgressBar = findViewById(R.id.update_progress);

        updateProgressBar.setVisibility(View.INVISIBLE);
        firebaseReference = FirebaseStorage.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS).child(firebaseUser.getUid());

//        Picasso.get().load(userImgUrl).into(imgShowProfile);
        Glide.with(getApplicationContext()).load(userImgUrl).into(imgShowProfile);
//        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        imgShowProfile.setOnLongClickListener(v -> {
            coverCheck = "profile";
            pickImage();
            return false;
        });

    }

//    private void askPermission(String permission, int requestCode) {
//        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            } else {
//                Toast.makeText(getApplicationContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent1 = result.getData();
                        if (intent1 != null) {
                            imgUri = intent1.getData();
                            updateProgressBar.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "Uploading...", Toast.LENGTH_SHORT).show();
                            uploadImageToDatabase();
                        }
                    }
                }
            }
    );

    private void uploadImageToDatabase() {
        StorageReference fileRef = firebaseReference.child(USER_IMAGE).child(String.format("%s.jpg", System.currentTimeMillis()));
        UploadTask uploadTask = fileRef.putFile(imgUri);

        uploadTask.continueWithTask(task -> {
           if (task.isSuccessful()) {
               return fileRef.getDownloadUrl();
           }
            return null;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                Log.d(TAG, String.valueOf(downloadUri));
                Map<String, Object> map = new HashMap<>();
                String url = downloadUri.toString();
                if (Objects.equals(coverCheck, "cover")) {
                    map.put("cover", url);
                    databaseReference.updateChildren(map);
                    coverCheck = "";
                } else if (Objects.equals(coverCheck, "profile")) {
                    map.put("profile", url);
                    databaseReference.updateChildren(map);
                    Picasso.get().load(url).placeholder(R.drawable.ic_profile).into(imgShowProfile);
                    coverCheck = "";
                }
                updateProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }
}