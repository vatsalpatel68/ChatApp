package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.lang.reflect.Type;

public class ShowAllUsers extends AppCompatActivity {

    RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    Query query;

    FirebaseRecyclerAdapter<Users , UsersViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_users);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ShowAllUsers.this));



        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .limitToLast(50);

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {


                    holder.tvNAME.setText(model.getName());
                    holder.tvSTATUS.setText(model.getStatus());
                    Picasso.get().load(model.getImage()).into(holder.ivUSERIMAGE);
                    //it is use to take a meta data.
                    final String u_id = getRef(position).getKey();
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ShowAllUsers.this, ShowUserProfile.class);
                            intent.putExtra("USER_ID", u_id);
                            startActivity(intent);
                        }
                    });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = getLayoutInflater().from(parent.getContext());
                View view = inflater.inflate(R.layout.users_single_layout,parent,false);
                return new UsersViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView tvNAME;
        TextView tvSTATUS;
        ImageView ivUSERIMAGE;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            tvNAME = itemView.findViewById(R.id.tvNAME);
            tvSTATUS = itemView.findViewById(R.id.tvSTATUS);
            ivUSERIMAGE = itemView.findViewById(R.id.ivUSERIMAGE);

        }
    }
}
