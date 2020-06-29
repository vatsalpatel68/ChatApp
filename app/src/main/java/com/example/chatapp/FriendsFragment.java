package com.example.chatapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ListIterator;


public class FriendsFragment extends Fragment {

    public FriendsFragment() {
        // Required empty public constructor
    }

    RecyclerView recyclerview_friends;
    View v;
    FirebaseUser firebaseUserForFriendsFragment;
    Query queryForFriends;
    FirebaseRecyclerAdapter<getStatusClass , getFriendsViewHolder> firebaseRecyclerAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerview_friends = v.findViewById(R.id.recylerview_friends);
        recyclerview_friends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerview_friends.setHasFixedSize(true);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUserForFriendsFragment = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUserForFriendsFragment != null)
        {
            new clearFriendsQueue().execute();

        }
    }



    //This will Initiate the Loading.
    public void setToRecycleView() {

        queryForFriends = FirebaseDatabase.getInstance()
                .getReference().child("Friends_queue")
                .child(firebaseUserForFriendsFragment.getUid())
                .limitToLast(50);

        FirebaseRecyclerOptions<getStatusClass> options =
                new FirebaseRecyclerOptions.Builder<getStatusClass>()
                        .setQuery(queryForFriends, getStatusClass.class)
                        .build();


        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<getStatusClass,getFriendsViewHolder>(options) {
            @NonNull
            @Override
            public getFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
                View view = inflater.inflate(R.layout.users_single_layout, parent, false);
                return new getFriendsViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull getFriendsViewHolder holder, int position, @NonNull getStatusClass model) {


                holder.tvNAME.setText(model.getName());
                holder.tvSTATUS.setText(model.getStatus().toString());
                Picasso.get().load(model.getImage().toString()).into(holder.ivUSERIMAGE);
                final String u_id = getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Add a OnClick event.
                        Intent intent = new Intent(getActivity().getApplicationContext(),ChatActivity.class);
                        intent.putExtra("User_id",u_id);
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerview_friends.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    class getFriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView ivUSERIMAGE;
        TextView tvNAME , tvSTATUS;
        public getFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            ivUSERIMAGE = itemView.findViewById(R.id.ivUSERIMAGE);
            tvNAME = itemView.findViewById(R.id.tvNAME);
            tvSTATUS = itemView.findViewById(R.id.tvSTATUS);
        }
    }



    public class clearFriendsQueue extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference referenceForClearTheFriendsQueue;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            referenceForClearTheFriendsQueue = FirebaseDatabase.getInstance().getReference()
                    .child("Frinds_queue").child(firebaseUserForFriendsFragment.getUid().toString());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            referenceForClearTheFriendsQueue.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    new getFriendsIds().execute();
                }
            });
            return null;
        }
    }

    public class getFriendsIds extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference referenceForGetAIDs;
        ArrayList<String> keys;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            referenceForGetAIDs = FirebaseDatabase.getInstance().getReference().child("Friends").child(firebaseUserForFriendsFragment.getUid().toString());
            keys = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            referenceForGetAIDs.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snap : dataSnapshot.getChildren())
                    {
                        keys.add(snap.getKey().toString());

                    }
                    new getDataFromtheUsers().execute(keys);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

    public class getDataFromtheUsers extends AsyncTask<ArrayList<String>,Void,Void>
    {

        DatabaseReference referenceForFindInUsers;
        HashMap<String,Object> map;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            referenceForFindInUsers = FirebaseDatabase.getInstance().getReference().child("users");
            map = new HashMap<>();

        }

        @Override
        protected Void doInBackground(ArrayList<String>... arrayLists) {

            ListIterator<String> itr = arrayLists[0].listIterator();
            while(itr.hasNext())
            {
                map.clear();
                referenceForFindInUsers.child(itr.next()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        map.put("name",dataSnapshot.child("name").getValue().toString());
                        map.put("status",dataSnapshot.child("status").getValue().toString());
                        map.put("image",dataSnapshot.child("image").getValue().toString());
                        map.put("Key",dataSnapshot.getKey().toString());
                        new AddToFriendQueue().execute(map);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            return null;
        }
    }


    public class AddToFriendQueue extends AsyncTask<HashMap<String,Object>,Void,Void>
    {

        DatabaseReference referenceForaddinFriendsQueue;
        HashMap<String,Object> newMap;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            referenceForaddinFriendsQueue = FirebaseDatabase.getInstance().getReference().child("Friends_queue").child(firebaseUserForFriendsFragment.getUid());
            newMap = new HashMap<>();
        }

        @Override
        protected Void doInBackground(HashMap<String, Object>... hashMaps) {
            newMap.put("name",hashMaps[0].get("name").toString());
            newMap.put("status",hashMaps[0].get("status").toString());
            newMap.put("image",hashMaps[0].get("image").toString());
            referenceForaddinFriendsQueue.child(hashMaps[0].get("Key").toString())
                    .setValue(newMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        //here All task to add to Friends Queue is Completed.
                        setToRecycleView();
                    }
                }
            });
            return null;
        }
    }
}
