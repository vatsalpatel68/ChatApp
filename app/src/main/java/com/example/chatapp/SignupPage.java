package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupPage extends AppCompatActivity {

    EditText etNAME , etMAIL , etPASSWORD;
    Button btnSUBMIT;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);
        etNAME = findViewById(R.id.etNAME);
        etMAIL = findViewById(R.id.etMAIL);
        etPASSWORD = findViewById(R.id.etPASSWORD);
        btnSUBMIT = findViewById(R.id.btnSUBMIT);
        mAuth = FirebaseAuth.getInstance();

        btnSUBMIT.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = etNAME.getText().toString().trim();
                String mail = etMAIL.getText().toString().trim();
                String password = etPASSWORD.getText().toString().trim();

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(mail) || TextUtils.isEmpty(password))
                {
                    Toast.makeText(getApplicationContext(),"Please Enter all the Data",Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog = new ProgressDialog(SignupPage.this);
                    progressDialog.setTitle("Add a user");
                    progressDialog.setMessage("Please wait for creating a account.");
                    progressDialog.show();
                    addUser(mail,password);
                }
            }
        });
    }

    private void addUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            new insertIntoDb().execute();
                            progressDialog.dismiss();
                           startActivity(new Intent(SignupPage.this,MainActivity.class));

                        } else {
                            progressDialog.dismiss();
                           Toast.makeText(getApplicationContext(),"There is a problem in insert a user.",Toast.LENGTH_SHORT).show();
                           etNAME.setText("");
                           etMAIL.setText("");
                           etPASSWORD.setText("");
                         }
                    }
                });


    }


    class insertIntoDb extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference databaseReference;
        FirebaseUser firebaseUser;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        }

        @Override
        protected Void doInBackground(Void... voids) {

            HashMap<String , Object> map = new HashMap<>();
            map.put("name",etNAME.getText().toString().trim());
            map.put("status","Hey!I am using ChatApp");
            map.put("image","https://firebasestorage.googleapis.com/v0/b/chatapp-af642.appspot.com/o/uploads%2Ft3pMwhDNfKPO7SCWnPpVlxo5CEv1.jpg?alt=media&token=a5d79a15-083d-4bfa-8578-17b09cec2c5b");
            map.put("thumb_image","default_person");
            databaseReference.setValue(map);
            return null;
        }
    }
}
