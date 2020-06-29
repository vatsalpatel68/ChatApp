package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowUserProfile extends AppCompatActivity {

    String USER_id;
    CircleImageView ivPROFILEIMG;
    TextView tvPROFILENAME , tvPROFILESTATUS;
    Button btnPROFILEBTN;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    Button Accept , Decline;
    Boolean acceptance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_profile);
        USER_id = getIntent().getStringExtra("USER_ID");
        ivPROFILEIMG = (CircleImageView) findViewById(R.id.ivPROFILEIMG);
        tvPROFILENAME = findViewById(R.id.tvPROFILENAME);
        tvPROFILESTATUS = findViewById(R.id.tvPROFILESTATUS);
        btnPROFILEBTN = findViewById(R.id.btnPROFILEBTN);
        Accept = findViewById(R.id.Accept);
        Decline = findViewById(R.id.Decline);
        btnPROFILEBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendRequestFunc().execute();
            }
        });

        //Database References.
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(USER_id);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        addData cla = new addData()
        {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        };
        cla.execute();



        //when User gets a Request then this two functions are useful as well as appear.
        Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptance = true;
                new requestResult().execute();
            }
        });


        Decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptance = false;
                new requestResult().execute();
            }
        });
    }

    class requestResult extends AsyncTask<Void,Void,Void>
    {

        DatabaseReference requestReference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            requestReference = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(firebaseUser.getUid())
                    .child(USER_id).child("status");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(acceptance) {
                requestReference.setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            new RequestResultForOpposite().execute();
                        }
                    }
                });
            }
            else
            {
                requestReference.setValue("Decline").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            new RequestResultForOpposite().execute();
                        }
                    }
                });
            }
            return null;
        }
    }

    class RequestResultForOpposite extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference requestReferencetwo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            requestReferencetwo = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(USER_id)
                    .child(firebaseUser.getUid()).child("status");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(acceptance) {
                requestReferencetwo.setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (acceptance) {
                                //When user Accept the request.
                                new insertIntoOneUserDb().execute();

                                //We craete a Instance in Chat.
                               // new insertintoOneUserChatDb().execute();
                                Accept.setVisibility(View.GONE);
                                Decline.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
            else
            {
                requestReferencetwo.setValue("Decline");
                Accept.setVisibility(View.GONE);
                Decline.setVisibility(View.GONE);
            }
            return null;
        }
    }

    class insertintoOneUserChatDb extends AsyncTask<Void,Void,Void>
    {

        DatabaseReference insertintoFriendreference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            insertintoFriendreference = FirebaseDatabase.getInstance().getReference().child("chats").child(firebaseUser.getUid()).child(USER_id);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            insertintoFriendreference.setValue("empty").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        new insertintoSecondUserChatDb().execute();
                    }
                }
            });
            return null;
        }
    }

    class insertintoSecondUserChatDb extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference insertIntoSecondUserReference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            insertIntoSecondUserReference = FirebaseDatabase.getInstance().getReference().child("chats")
                    .child(USER_id).child(firebaseUser.getUid());

        }

        @Override
        protected Void doInBackground(Void... voids) {
            insertIntoSecondUserReference.setValue("empty").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
            return null;
        }

    }

    class insertIntoOneUserDb extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference insertintoFriendreference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            insertintoFriendreference = FirebaseDatabase.getInstance().getReference().child("Friends").child(firebaseUser.getUid()).child(USER_id);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            insertintoFriendreference.setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        new insertIntoSecondUserDb().execute();
                    }
                }
            });
            return null;
        }
    }

    class insertIntoSecondUserDb extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference insertIntoSecondUserReference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            insertIntoSecondUserReference = FirebaseDatabase.getInstance().getReference().child("Friends")
                    .child(USER_id).child(firebaseUser.getUid());

        }

        @Override
        protected Void doInBackground(Void... voids) {
            insertIntoSecondUserReference.setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"Add to Friend",Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
    }










    //**********************************************************************************************************************
    class sendRequestFunc extends AsyncTask<Void,Void,Void>
    {
        DatabaseReference requestreferenceone;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            requestreferenceone = FirebaseDatabase.getInstance().getReference().child("Friend_request")
                    .child(firebaseUser.getUid()).child(USER_id)
                    .child("status");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            requestreferenceone.setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        new sendRequestFunc2().execute();
                    }
                }
            });
            return null;
        }
    }



    class sendRequestFunc2 extends AsyncTask<Void,Void,Void>
    {

        DatabaseReference requestreferencetwo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            requestreferencetwo = FirebaseDatabase.getInstance().getReference().child("Friend_request")
                    .child(USER_id).child(firebaseUser.getUid())
                    .child("status");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            requestreferencetwo.setValue("Receive").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Friend Request Sent",Toast.LENGTH_SHORT).show();
                        btnPROFILEBTN.setBackgroundColor(Color.GREEN);
                        btnPROFILEBTN.setText("Sent!");
                        btnPROFILEBTN.setEnabled(false);
                        btnPROFILEBTN.setTextColor(Color.WHITE);
                    }
                }
            });
            return null;
        }
    }



    //This is for Showing User data.
  class addData extends AsyncTask<Void,Void,Void>
  {

      @Override
      protected Void doInBackground(Void... voids) {
          ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  tvPROFILENAME.setText(dataSnapshot.child("name").getValue().toString());
                  tvPROFILESTATUS.setText(dataSnapshot.child("status").getValue().toString());
                  Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(ivPROFILEIMG);
                  String str1 = dataSnapshot.getKey().toString();

                  new CheckButtonVisibility().execute(str1);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });

          return null;
      }
  }

  class CheckFriendRequestStatus extends AsyncTask<Void, Void, Void>
  {
        DatabaseReference checkRequestStatus;
      @Override
      protected void onPreExecute() {
            checkRequestStatus = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(firebaseUser.getUid())
                    .child(USER_id)
                    .child("status");
          super.onPreExecute();
      }


      @Override
      protected Void doInBackground(Void... voids) {
          if(checkRequestStatus != null) {
              checkRequestStatus.addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      if(dataSnapshot.exists())
                      {
                          Log.e("KEYIS",dataSnapshot.getValue().toString());
                          if(dataSnapshot.getValue().toString().equals("sent")) {
                              btnPROFILEBTN.setBackgroundColor(Color.GREEN);
                              btnPROFILEBTN.setText("Sent!");
                              btnPROFILEBTN.setEnabled(false);
                              btnPROFILEBTN.setTextColor(Color.WHITE);
                          }
                          if(dataSnapshot.getValue().toString().equals("accepted"))
                          {
                              btnPROFILEBTN.setBackgroundColor(Color.GREEN);
                              btnPROFILEBTN.setText("You are already Friend");
                              btnPROFILEBTN.setEnabled(false);
                              btnPROFILEBTN.setTextColor(Color.WHITE);
                          }
                          if(dataSnapshot.getValue().toString().equals("Decline"))
                          {
                              btnPROFILEBTN.setText("Send a Friend Request");
                              btnPROFILEBTN.setEnabled(true);
                          }
                      }
                      else
                      {

                      }

                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
          }
            return null;
      }

  }

class CheckFriendRequestStatusTwo extends AsyncTask<Void,Void,Void>
{
    DatabaseReference databaseReferenceforaccept;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        databaseReferenceforaccept = FirebaseDatabase.getInstance().getReference()
                .child("Friend_request").child(firebaseUser.getUid()).child(USER_id)
                .child("status");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(databaseReferenceforaccept != null)
        {
            databaseReferenceforaccept.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Log.e("KEYIS",dataSnapshot.getValue().toString());
                        if(dataSnapshot.getValue().toString().equals("Receive")) {
                            btnPROFILEBTN.setVisibility(View.GONE);
                            Accept.setVisibility(View.VISIBLE);
                            Decline.setVisibility(View.VISIBLE);
                        }
                        if(dataSnapshot.getValue().toString().equals("accepted"))
                        {
                            btnPROFILEBTN.setBackgroundColor(Color.GREEN);
                            btnPROFILEBTN.setText("You are already Friend");
                            btnPROFILEBTN.setEnabled(false);
                            btnPROFILEBTN.setTextColor(Color.WHITE);
                        }
                        if(dataSnapshot.getValue().toString().equals("Decline"))
                        {
                            btnPROFILEBTN.setText("Send a Friend Request");
                            btnPROFILEBTN.setEnabled(true);
                        }
                        }
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}

  //This is for Checking Button Show or Not.
  class CheckButtonVisibility extends AsyncTask<String,Void,String>
  {
        String str2;
      @Override
      protected String doInBackground(String... strings) {
          str2 = firebaseUser.getUid().toString();
          return strings[0];
      }

      @Override
      protected void onPostExecute(String s) {
          if(s.equals(str2))
          {
              btnPROFILEBTN.setVisibility(View.GONE);
          }
          else
          {
              btnPROFILEBTN.setVisibility(View.VISIBLE);
              new CheckFriendRequestStatus().execute();
              new CheckFriendRequestStatusTwo().execute();
          }
          super.onPostExecute(s);
      }
  }

}
