package com.app.emaneraky.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    TextView displayName, displaySatus;
    CircleImageView displayProfile;
    Button changeImage_btn, changestatus_btn;
    private FirebaseUser currentUser;
    private DatabaseReference mUserDatabase;
    private static final int galarry_PIC = 1;
    private StorageReference mImageStorageRef;
    private static final int MAX_LENGTH = 15;
    private ProgressDialog mProgressDialog;
    String current_user_id;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        displayName = (TextView) findViewById(R.id.set_displayName);
        displaySatus = (TextView) findViewById(R.id.set_displayStatus);
        displayProfile = (CircleImageView) findViewById(R.id.setting_profile);
        changestatus_btn = (Button) findViewById(R.id.change_status);
        changeImage_btn = (Button) findViewById(R.id.change_image);

        mImageStorageRef = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = currentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        mUserDatabase.keepSynced(true); //offline capability

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                displayName.setText(name);
                displaySatus.setText(status);
                if (!image.equals("default")) {
//                    Picasso.with(SettingActivity.this).load(image).into(displayProfile);

                    Picasso.with(SettingActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(displayProfile, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            //in case online
                            Picasso.with(SettingActivity.this).load(image).into(displayProfile);

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        changestatus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = displaySatus.getText().toString();
                Intent statusIntent = new Intent(SettingActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
//                finish();
            }
        });
        changeImage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallaryIntent, "Select Image"), galarry_PIC);

                // start picker to get image for cropping and then use the image in cropping activity
//                // option to choice image from gallary or take image by camera and crop it
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingActivity.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == galarry_PIC && resultCode == RESULT_OK) {
            Uri resultUri = data.getData();
            CropImage.activity(resultUri).
                    setAspectRatio(1, 1).
                    setMinCropResultSize(20, 200)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(SettingActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please Wait for UpLoading....");
                mProgressDialog.show();

                final Uri resultUri = result.getUri();
                File thumbin_path = new File(resultUri.getPath());

////this for compress image to load fast
//                Bitmap thumbin_ImageBitmap = new Compressor(this)
//                        .setMaxWidth(640)
//                        .setMaxHeight(480)
//                        .setQuality(75).compressToBitmap(thumbin_path);
//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                thumbin_ImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] thumb_byte = baos.toByteArray();
//                StorageReference thumb_filepath = mImageStorageRef.child("profile_images").child("thumbs_profile_image").child(current_user_id + ".jpg");
////.....................................................

                StorageReference filepath = mImageStorageRef.child("profile_images").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingActivity.this, "Fail to UpLoading", Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String downLoad_Url = downloadUrl.toString();
                        mUserDatabase.child("image").setValue(downLoad_Url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgressDialog.dismiss();
                                Toast.makeText(SettingActivity.this, "Success UpLoading", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    // create random string names
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
