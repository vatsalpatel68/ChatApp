package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileAct extends AppCompatActivity {

    Button btnCHANGEPIC , btnCHANGESTATUS;
    TextView userNAME , userSTATUS;
    CircleImageView circleImageView;
    Uri Imageuri , uri;
    StorageReference mStorageRef;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    final static int GALLARY_PICK = 1;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        btnCHANGEPIC = findViewById(R.id.btnCHANGEPIC);
        btnCHANGESTATUS = findViewById(R.id.btnCHANGESTATUS);
        userNAME = findViewById(R.id.userNAME);
        userSTATUS = findViewById(R.id.userSTATUS);
        circleImageView = ( CircleImageView ) findViewById(R.id.circleImageView);
        new loadData().execute();

        btnCHANGESTATUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(profileAct.this,changestatus.class));
            }
        });


        btnCHANGEPIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImageFunction();
            }
        });
    }

    private void setImageFunction() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,GALLARY_PICK);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLARY_PICK && resultCode == RESULT_OK)
        {

                 Imageuri = data.getData();

            CropImage.activity(Imageuri)
                    .setAspectRatio(1,1)
                    .start(profileAct.this);


        }

        //Result after Cropping.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();
                addImageToDB();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }



    }

    private void addImageToDB() {
        dialog = new ProgressDialog(profileAct.this);
        dialog.setTitle("Uploading...");
        dialog.setMessage("Please wait while uploading");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("uploads").child(firebaseUser.getUid() + ".jpg");
        mStorageRef.putFile(uri)
         .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {
                         String url = uri.toString();
                         updateProfileImage(url);
                     }
                 });

             }
         });
    }



    private void updateProfileImage(String url) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("image");

        databaseReference.setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    dialog.dismiss();
                }
            }
        });

    }








    //it will handle back button pressed.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(profileAct.this,MainActivity.class);
        startActivity(a);
    }

    class loadData extends AsyncTask<Void, Void, Void>
    {
        FirebaseUser firebaseUser;
        DatabaseReference databaseReference;
        String name;
        String status;
        String image;
        String thumb_image;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        }


        @Override
        protected Void doInBackground(Void... voids) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString().trim();
                    status = dataSnapshot.child("status").getValue().toString().trim();
                    image = dataSnapshot.child("image").getValue().toString().trim();
                    thumb_image = dataSnapshot.child("thumb_image").getValue().toString().trim();
                    userNAME.setText(name);
                    userSTATUS.setText(status);


                    Picasso.get().load(image).into(circleImageView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }
    }

