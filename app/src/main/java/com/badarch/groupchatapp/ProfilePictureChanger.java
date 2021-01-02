package com.badarch.groupchatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfilePictureChanger extends AppCompatActivity implements View.OnClickListener {

    private Uri imageUri;
    private String userID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final int IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedI) {
        super.onCreate(savedI);
        setContentView(R.layout.profile_picture_change);
        Button btn = findViewById(R.id.change_profile_pic);
        Button btn2 = findViewById(R.id.back_to_profile);
        Button btn3 = findViewById(R.id.save_user_picture);
        mAuth = FirebaseAuth.getInstance();
        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String pathOfPic = extras.getString("path");

        loadImage(pathOfPic);

    }

    private void loadImage(String path) {

        ImageView img = findViewById(R.id.cachedImage);
        img.setImageResource(R.drawable.ic_account);

        try {
            File f = new File(path, FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageBitmap(b);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_profile_pic:
                getImage();
                break;
            case R.id.back_to_profile:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.save_user_picture:
                uploadImage();
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getImage() {

        if (checkingReq()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkingReq() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return false;
        }
    }


    @Override
    protected void onActivityResult(int reqcode, int rescode, Intent data) {
        super.onActivityResult(reqcode, rescode, data);
        if (data != null) {
            if (reqcode == IMAGE_REQUEST && rescode == RESULT_OK) {
                imageUri = data.getData();


                ImageView img = findViewById(R.id.cachedImage);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setImageURI(imageUri);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void uploadImage() {
        if (imageUri != null) {
            userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.custom_dialog, null));
            builder.setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.show();

            final StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(userID).child("userIcon");
            final Intent intent = new Intent(this, MainActivity.class);

            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseFirestore mStore = FirebaseFirestore.getInstance();

                            Map<String, Object> mStoreUser = new HashMap<>();

                            mStoreUser.put("userIcon", uri.toString());
                            mStore.document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .update(mStoreUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                String editUri = String.valueOf(imageUri);
                                                intent.putExtra("imageURI", editUri);
                                                setResult(1, intent);
                                                finish();
                                            }
                                        });
                        }
                    });

                }
            });
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
