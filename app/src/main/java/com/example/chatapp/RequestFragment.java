package com.example.chatapp;

import android.content.Context;
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
import java.util.Iterator;


public class RequestFragment extends Fragment {


    public RequestFragment() {
        // Required empty public constructor
    }

    View v;
    RecyclerView rvLOADREQUEST;
    FirebaseUser firebaseUser;
    Query queryForRequest;
    FirebaseRecyclerAdapter<getStatusClass , getRequestViewHolder> firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_request, container, false);
        rvLOADREQUEST = v.findViewById(R.id.rvLOADREQUEST);
        rvLOADREQUEST.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        rvLOADREQUEST.setHasFixedSize(true);


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            new RequestQueueforShowingToFragment().execute();

        }
    }


    //This will Initiate the Loading.
       public void setToRecycleView() {

           queryForRequest = FirebaseDatabase.getInstance()
                   .getReference().child("Request_queue_with_data")
                   .child(firebaseUser.getUid())
                   .limitToLast(50);

           FirebaseRecyclerOptions<getStatusClass> options =
                   new FirebaseRecyclerOptions.Builder<getStatusClass>()
                           .setQuery(queryForRequest, getStatusClass.class)
                           .build();


           firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<getStatusClass, getRequestViewHolder>(options) {
               @NonNull
               @Override
               public getRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                   LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
                   View view = inflater.inflate(R.layout.users_single_layout, parent, false);
                   return new getRequestViewHolder(view);

               }

               @Override
               protected void onBindViewHolder(@NonNull getRequestViewHolder holder, int position, @NonNull getStatusClass model) {

                   Log.e("KEYIS2", "This is called");
                   holder.tvNAME.setText(model.getName());
                   holder.tvSTATUS.setText(model.getStatus().toString());
                   Picasso.get().load(model.getImage().toString()).into(holder.ivUSERIMAGE);
                   final String u_id = getRef(position).getKey();
                   holder.mView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent intent = new Intent(getActivity().getApplicationContext(), ShowUserProfile.class);
                           intent.putExtra("USER_ID", u_id);
                           startActivity(intent);
                       }
                   });
               }
           };
           rvLOADREQUEST.setAdapter(firebaseRecyclerAdapter);
           firebaseRecyclerAdapter.startListening();
       }

    class getRequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView ivUSERIMAGE;
        TextView tvNAME , tvSTATUS;
        public getRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            ivUSERIMAGE = itemView.findViewById(R.id.ivUSERIMAGE);
            tvNAME = itemView.findViewById(R.id.tvNAME);
            tvSTATUS = itemView.findViewById(R.id.tvSTATUS);
        }
    }


    //here start to insert into the Queue.
    public class RequestQueueforShowingToFragment extends AsyncTask<Void,Void,Void>
    {

        DatabaseReference refenceforQueueForRequstFragment;
        ArrayList<String> keys;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refenceforQueueForRequstFragment = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(firebaseUser.getUid());
            keys = new ArrayList<String>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            refenceforQueueForRequstFragment.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    keys.clear();
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(snap.child("status").getValue().toString().equals("Receive")) {
                            Log.e("KEYIS","check status function complete work");
                            keys.add(snap.getKey().toString());
                        }
                    }
                    //Before adding a data we need to delete previous ones.after that task is execute.

                    new DeletePreviousDataBeforeAdd().execute();
                    }



                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }

        public class DeletePreviousDataBeforeAdd extends AsyncTask<Void,Void,Void>
        {

            DatabaseReference deleteFromtheRequestQueue;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                deleteFromtheRequestQueue = FirebaseDatabase.getInstance().getReference()
                        .child("Request_queue").child(firebaseUser.getUid());
            }

            @Override
            protected Void doInBackground(Void... voids) {
                deleteFromtheRequestQueue.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        new DeleteRequestQueueWithData().execute();

                    }
                });
                return null;
            }

        }

        public class DeleteRequestQueueWithData extends AsyncTask<Void,Void,Void>
        {
            DatabaseReference databaseReferencefroDeletingaUsersDataFromRequestQueue;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                databaseReferencefroDeletingaUsersDataFromRequestQueue = FirebaseDatabase.getInstance().getReference()
                        .child("Request_queue_with_data").child(firebaseUser.getUid().toString());
            }

            @Override
            protected Void doInBackground(Void... voids) {
                databaseReferencefroDeletingaUsersDataFromRequestQueue.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            new addKeysToRequestQueue().execute();
                        }
                    }
                });
                return null;
            }
        }

        public class addKeysToRequestQueue extends AsyncTask<Void,Void,Void>
        {

            DatabaseReference addKeysToRequestQueueReference;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                addKeysToRequestQueueReference = FirebaseDatabase.getInstance().getReference().child("Request_queue")
                        .child(firebaseUser.getUid());
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Iterator<String> itr = keys.iterator();
                while(itr.hasNext())
                {

                    addKeysToRequestQueueReference.child(itr.next().toString()).setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            new addUsersDatatoRequestQueue().execute();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);

            }
        }


        public class addUsersDatatoRequestQueue extends AsyncTask<Void,Void,Void>
        {

            DatabaseReference referenceforinsertintorequestqueue;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                referenceforinsertintorequestqueue = FirebaseDatabase.getInstance().getReference().child("Request_queue").child(firebaseUser.getUid());
            }

            @Override
            protected Void doInBackground(Void... voids) {
                referenceforinsertintorequestqueue.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snap : dataSnapshot.getChildren())
                        {

                            new InsertDataIntoRequestQueue().execute(snap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return null;
            }
        }




        public class InsertDataIntoRequestQueue extends AsyncTask<DataSnapshot,Void,Void>
        {
            DatabaseReference databaseReferenceforInsertinIntoQueue;
            DatabaseReference databaseReferenceforInsertIntoRequestQueueaData;
            DatabaseReference forReference;
            HashMap<String,Object> dataFromDb;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                databaseReferenceforInsertinIntoQueue = FirebaseDatabase.getInstance().getReference()
                        .child("users");

                //if Data is Changed Then we Required To Delete all The data.
                databaseReferenceforInsertIntoRequestQueueaData = FirebaseDatabase.getInstance().getReference()
                        .child("Request_queue_with_data").child(firebaseUser.getUid());
                    dataFromDb = new HashMap<>();
            }

            @Override
            protected Void doInBackground(final DataSnapshot... dataSnapshotsFromUpper) {
                forReference = databaseReferenceforInsertIntoRequestQueueaData.child(dataSnapshotsFromUpper[0].getKey().toString());
                databaseReferenceforInsertinIntoQueue.child(dataSnapshotsFromUpper[0].getKey().toString())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                dataFromDb.clear();
                                dataFromDb.put("name",dataSnapshot.child("name").getValue().toString());
                                dataFromDb.put("image",dataSnapshot.child("image").getValue().toString());
                                dataFromDb.put("status",dataSnapshot.child("status").getValue().toString());
                                new sampleClass().execute(dataFromDb);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                Log.e("KEYIS",dataSnapshotsFromUpper[0].getKey().toString());
                return null;
            }


            class sampleClass extends AsyncTask<HashMap<String,Object>,Void,Void>
            {

                @Override
                protected Void doInBackground(HashMap<String, Object>... hashMaps) {
                    forReference.setValue(hashMaps[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Log.e("KEYIS","Operation Done");
                                    setToRecycleView();
                                }
                        }
                    });
                    return null;
                }
            }



        }
    }
    //add to Queue Operation is Ends here.
    //Now,We have to add Users Data from the 'users' child.


}
