package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.TimeUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;

public class ChatActivity extends AppCompatActivity {

    FirebaseUser firebaseUser_in_chat;
    String user_id;
    ActionBar actionBar;
    RecyclerView recyclerview_chat;
    EditText etINPUT;
    Button btnSEND;
    Query queryForChat;
    String send_message;
    FirebaseRecyclerAdapter< getChatClass , ChatClassViewHolder > firebaseRecyclerAdapter;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseUser_in_chat = FirebaseAuth.getInstance().getCurrentUser();
        actionBar = getSupportActionBar();
        recyclerview_chat = findViewById(R.id.recycleview_chat);
        etINPUT = findViewById(R.id.etINPUT);
        btnSEND = findViewById(R.id.btnSEND);
        user_id = getIntent().getStringExtra("User_id");
        recyclerview_chat.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview_chat.setHasFixedSize(true);
        new getUserData().execute();

        btnSEND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrNot();
            }
        });

    }

    public void  sendOrNot(){
        send_message = etINPUT.getText().toString();
        if(!TextUtils.isEmpty(send_message))
        {
            new sendMessageToReceiver().execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please enter a message.",Toast.LENGTH_SHORT).show();
        }
    }

    public class sendMessageToReceiver extends AsyncTask<Void,Void,Void>
    {

        DatabaseReference reference_sender;
        HashMap<String,Object> map;
        String seconds , year;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reference_sender = FirebaseDatabase.getInstance().getReference().child("chats")
                    .child(firebaseUser_in_chat.getUid().toString())
                    .child(user_id);
            map = new HashMap<>();
            seconds  =  String.valueOf(Calendar.getInstance().getTimeInMillis());
            year = String.valueOf(Calendar.getInstance().getWeekYear());

        }


        @Override
        protected Void doInBackground(Void... voids) {
            map.put("data",send_message);
            map.put("status","send");

            reference_sender.child(seconds + year).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Insert into Sender User Db is completed.Now,on Receiver Db.
                    new enterIntoReceiverChat().execute();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            firebaseRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public class enterIntoReceiverChat extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference reference_receiver;
        HashMap<String,Object> map;

        String seconds,year;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reference_receiver = FirebaseDatabase.getInstance().getReference().child("chats")
                    .child(user_id)
                    .child(firebaseUser_in_chat.getUid().toString());
            map = new HashMap<>();
            seconds = String.valueOf(Calendar.getInstance().getTimeInMillis());
            year = String.valueOf(Calendar.getInstance().getWeekYear());



        }


        @Override
        protected Void doInBackground(Void... voids) {
            map.put("data",send_message);
            map.put("status","receive");
            reference_receiver.child(seconds + year).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Insert into Receiver  Db is completed.
                    setToRecyclerView();
                    etINPUT.setText("");
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public void setToRecyclerView()
    {
        queryForChat = FirebaseDatabase.getInstance()
                .getReference().child("chats")
                .child(firebaseUser_in_chat.getUid())
                .child(user_id)
                .limitToLast(50);


        FirebaseRecyclerOptions<getChatClass> options =
                new FirebaseRecyclerOptions.Builder<getChatClass>()
                        .setQuery(queryForChat, getChatClass.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<getChatClass, ChatClassViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatClassViewHolder holder, int position, @NonNull getChatClass model) {
                if(model.getStatus().equals("send")){
                    holder.tvChat.setText(model.getData().toString());
                    holder.tvChatName.setText("You");
                }
                else
                {
                    holder.tvChat.setText(model.getData().toString());
                    holder.tvChatName.setText(name);
                }
            }

            @NonNull
            @Override
            public ChatClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = getLayoutInflater().from(getApplicationContext());
                View v = inflater.inflate(R.layout.chat_layout,parent,false);
                return new ChatClassViewHolder(v);
            }
        };
        recyclerview_chat.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public class ChatClassViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView tvChat , tvChatName;
        public ChatClassViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            tvChat  = itemView.findViewById(R.id.tvChat);
            tvChatName = itemView.findViewById(R.id.tvChatName);
        }
    }

    //This class is useful for change the ActionBar title.
    public class getUserData extends AsyncTask<Void,Void,Void>
    {

        DatabaseReference chat_fetch_User_data;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            chat_fetch_User_data = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(user_id);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            chat_fetch_User_data.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString();
                    actionBar.setTitle(dataSnapshot.child("name").getValue().toString());
                    setToRecyclerView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }
}
