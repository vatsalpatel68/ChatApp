package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class changestatus extends AppCompatActivity {


    EditText etCHANGESTATUS;
    Button setSTATUS;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changestatus);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        etCHANGESTATUS = findViewById(R.id.etCHANGESTATUS);
        setSTATUS = findViewById(R.id.setSTATUS);

        setSTATUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changestatusfunc();
            }
        });
    }

    private void changestatusfunc() {
        String newStatus = etCHANGESTATUS.getText().toString().trim();
        if(TextUtils.isEmpty(newStatus))
        {
            Toast.makeText(getApplicationContext(),"Please enter a new Status",Toast.LENGTH_SHORT).show();
        }
        else
        {
            changeStatus(newStatus);
        }
    }

    private void changeStatus(String newStatus) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        databaseReference.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    startActivity(new Intent(changestatus.this,profileAct.class));
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Status Not changed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
