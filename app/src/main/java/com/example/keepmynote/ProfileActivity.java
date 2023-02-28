package com.example.keepmynote;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView circleImageView;
    TextInputEditText displaynameedt;
    Button updateprofilebutton;
    ProgressBar progressBar;

    String DISPLAY_NAME = null;

    StorageReference storageReference, profileRef;
    FirebaseUser user;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        circleImageView=findViewById(R.id.profile_image);
        displaynameedt=findViewById(R.id.displaynameedt);
        updateprofilebutton=findViewById(R.id.updateprofilebutton);
        progressBar=findViewById(R.id.progressBar);

        storageReference = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user !=null){
            Log.d(TAG, "onCreate: "+user.getDisplayName());

            //get their registered name
            if (user.getDisplayName() !=null){
                displaynameedt.setText(user.getDisplayName());
                displaynameedt.setSelection(user.getDisplayName().length());

            }
            //get their uploaded photo
            profileRef = storageReference.child(user.getUid()+".jpeg");
            profileRef.getDownloadUrl()
                    .addOnSuccessListener(uri ->
                            Picasso.get().load(uri).into(circleImageView));
        }

        //if user want to change the photo, click the imageview and go to the media gallery
        circleImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if users selected the photo, the photo will be given specific url
        if(requestCode == 1000 && resultCode == Activity.RESULT_OK){
                assert data != null;
                Uri imageUri = data.getData();

        //once completed, the photo will be uploaded to firebase storage
            uploadImagetoFirebase(imageUri);
        }
    }

    private void uploadImagetoFirebase(Uri imageUri) {

        StorageReference fileRef = storageReference.child(user.getUid()+".jpeg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "onSuccess: ");
                    Toast.makeText(ProfileActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();

                    //once uploaded to firebase storage, the image will be retrieved by android imageview
                    fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri ->
                                    Picasso.get().load(uri).into(circleImageView));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e.getCause());
                    Toast.makeText(ProfileActivity.this, "Image failed to upload", Toast.LENGTH_SHORT).show();

                });

    }

    public void updateProfile(View view) {

        view.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        DISPLAY_NAME = Objects.requireNonNull(displaynameedt.getText()).toString();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(DISPLAY_NAME)
                .build();

        assert firebaseUser != null;
        firebaseUser.updateProfile(request)
                .addOnSuccessListener(unused -> {
                    view.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Successfully updated profile name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onSuccess: ");
                })
                .addOnFailureListener(e -> {
                    view.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onFailure: ", e.getCause());
                });
    }

}