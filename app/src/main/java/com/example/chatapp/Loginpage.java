package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Loginpage extends AppCompatActivity {

    EditText etMAIL , etPASSWORD;
    Button btnLOGIN;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        etMAIL = findViewById(R.id.etMAIL);
        etPASSWORD = findViewById(R.id.etPASSWORD);
        btnLOGIN = findViewById(R.id.btnLOGIN);
        mAuth = FirebaseAuth.getInstance();

        btnLOGIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = etMAIL.getText().toString().trim();
                String password = etPASSWORD.getText().toString().trim();

                if(TextUtils.isEmpty(mail) || TextUtils.isEmpty(password))
                {
                    Toast.makeText(getApplicationContext(),"Please enter all the data",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog = new ProgressDialog(Loginpage.this);
                    progressDialog.setTitle("Login");
                    progressDialog.setMessage("Please wait while login");
                    progressDialog.show();
                    loginAct(mail,password);
                }

            }
        });
    }

    private void loginAct(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(Loginpage.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                            etMAIL.setText("");
                            etPASSWORD.setText("");
                            progressDialog.dismiss();
                        }
                    }
                });

    }

}
