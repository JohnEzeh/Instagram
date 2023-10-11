package com.example.instagram;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.instagram.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.time.chrono.MinguoEra;
import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
    private static final int GALLERYCODED = 7;
    ImageView img_close,image_pro;
    TextView txt_save,txt_change_photo;
    MaterialEditText full_name,user_name,et_bio;

    FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        img_close = findViewById(R.id.img_close);
        image_pro = findViewById(R.id.image_pro);
        txt_save = findViewById(R.id.txt_save);
        txt_change_photo = findViewById(R.id.txt_change_photo);
        full_name = findViewById(R.id.full_name);
        user_name = findViewById(R.id.user_name);
        et_bio = findViewById(R.id.et_bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                full_name.setText(user.getFullname());
                user_name.setText(user.getUsername());
                et_bio.setText(user.getBio());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_pro);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        img_close.setOnClickListener(view -> {
            finish();
        });

        txt_change_photo.setOnClickListener(view -> {
            pickImager();
        });

        image_pro.setOnClickListener(view -> {
            pickImager();
        });

        txt_save.setOnClickListener(view -> {

            updateProfile(full_name.getText().toString(),user_name.getText().toString(),et_bio.getText().toString());
            uploadImage();
        });

    }

    private void updateProfile(String fullname, String username, String bio) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullname", fullname);
        hashMap.put("username", username);
        hashMap.put("bio", bio);

        reference.updateChildren(hashMap);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("uploading");
        pd.show();

        if (mImageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    +"." +getFileExtension(mImageUri));

            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWith(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("imageurl", ""+myUrl);

                                reference.updateChildren(hashMap);
                                pd.dismiss();
                            } else {

                        String errorMsg = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(EditProfileActivity.this, "Failed due to"+errorMsg, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, "failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERYCODED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERYCODED && resultCode == RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                image_pro.setImageURI(mImageUri);

                //uploadImage();
            } else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
            //uploadImage();
        }
    }
}