package com.example.chatapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ChatsFragment extends Fragment {

    public ChatsFragment() {
        // Required empty public constructor
    }

    View view;
    RecyclerView Recycler_view_Chat;
    Query queryForChats;
    FirebaseUser firebaseUser;
    FirebaseRecyclerAdapter<getStatusClass, getChatViewHolder> firebaseRecyclerAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        Recycler_view_Chat = view.findViewById(R.id.Recycler_view_Chat);
        Recycler_view_Chat.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        Recycler_view_Chat.setHasFixedSize(true);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        new getFriendIds().execute();
        return view;
    }


    public void SetToRecyleView(){

        queryForChats = FirebaseDatabase.getInstance()
                .getReference().child("Chat_queue_with_data")
                .child(firebaseUser.getUid())
                .limitToLast(50);

        FirebaseRecyclerOptions<getStatusClass> options =
                new FirebaseRecyclerOptions.Builder<getStatusClass>()
                        .setQuery(queryForChats , getStatusClass.class)
                        .build();


        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<getStatusClass, getChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull getChatViewHolder holder, int position, @NonNull getStatusClass model) {
                holder.tvNAME.setText(model.getName().toString());
                holder.tvSTATUS.setText(model.getStatus().toString());
                final String u_id = getRef(position).getKey();
                Picasso.get().load(model.getImage().toString()).into(holder.ivUSERIMAGE);

                holder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
                        intent.putExtra("User_id", u_id);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public getChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
                View v = inflater.inflate(R.layout.users_single_layout,parent,false);
                return new getChatViewHolder(v);
            }
        };
        Recycler_view_Chat.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public class getChatViewHolder extends RecyclerView.ViewHolder
    {

        View mview;
        ImageView ivUSERIMAGE;
        TextView tvNAME , tvSTATUS;
        public getChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
            ivUSERIMAGE = itemView.findViewById(R.id.ivUSERIMAGE);
            tvNAME = itemView.findViewById(R.id.tvNAME);
            tvSTATUS = itemView.findViewById(R.id.tvSTATUS);
        }
    }


    public class getFriendIds extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference Ref_for_getIds;
        ArrayList<String> keys;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Ref_for_getIds = FirebaseDatabase.getInstance().getReference()
                    .child("chats").child(firebaseUser.getUid().toString());

            keys = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Ref_for_getIds.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    keys.clear();
                    for(DataSnapshot snap : dataSnapshot.getChildren())
                    {
                        keys.add(snap.getKey().toString());


                    }
                    new ClearTheTree().execute(keys);
                    //new getDataFromUsers().execute(keys);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

    public class ClearTheTree extends AsyncTask<ArrayList<String>,Void,Void>
    {
        DatabaseReference Ref_for_clear_tree;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Ref_for_clear_tree = FirebaseDatabase.getInstance().getReference()
                    .child("Chat_queue_with_data").child(firebaseUser.getUid());
        }

        @Override
        protected Void doInBackground(final ArrayList<String>... arrayLists) {
            Ref_for_clear_tree.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        new getDataFromUsers().execute(arrayLists[0]);
                    }
                }
            });
            return null;
        }

    }


    public class getDataFromUsers extends AsyncTask<ArrayList<String>,Void,Void>
    {
        DatabaseReference Ref_for_fetch_info;
        HashMap<String,Object> map;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Ref_for_fetch_info = FirebaseDatabase.getInstance().getReference()
                    .child("users");
            map = new HashMap<>();
        }

        @Override
        protected Void doInBackground(ArrayList<String>... arrayLists) {
            Iterator<String> itr  = arrayLists[0].listIterator();
            while(itr.hasNext())
            {
                map.clear();
                Ref_for_fetch_info.child(itr.next()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        map.put("name",dataSnapshot.child("name").getValue().toString());
                        map.put("image",dataSnapshot.child("image").getValue().toString());
                        map.put("status",dataSnapshot.child("status").getValue().toString());
                        map.put("key",dataSnapshot.getKey().toString());
                        new setDataToTree().execute(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            return null;
        }
    }

    public class setDataToTree extends AsyncTask<HashMap<String,Object>,Void,Void>
    {
        DatabaseReference Ref_for_insert;
        HashMap<String ,Object> newMap;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Ref_for_insert = FirebaseDatabase.getInstance().getReference()
                    .child("Chat_queue_with_data").child(firebaseUser.getUid());
            newMap = new HashMap<>();
        }

        @Override
        protected Void doInBackground(HashMap<String, Object>... hashMaps) {
            newMap.put("name",hashMaps[0].get("name").toString());
            newMap.put("image",hashMaps[0].get("image").toString());
            newMap.put("status",hashMaps[0].get("status").toString());
            Ref_for_insert.child(hashMaps[0].get("key").toString()).setValue(newMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                               SetToRecyleView();
                            }
                        }
                    });
            return null;
        }
    }
}
