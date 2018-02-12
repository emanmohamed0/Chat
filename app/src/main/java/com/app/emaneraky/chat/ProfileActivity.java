package com.app.emaneraky.chat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    ImageView pro_Image;
    TextView profile_Name, profile_Status, total_Friends;
    Button sendRequest, declineRequest;
    private DatabaseReference mUserDatabaseDetail;
    ProgressDialog dialog;
    private String current_state;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendReceiveDatabase;
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    String user_id;
    private DatabaseReference mNotificationDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user_id = getIntent().getStringExtra("user_id");

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading User Data");
        dialog.setMessage("Please Wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        current_state = "not_friends";


        pro_Image = (ImageView) findViewById(R.id.profile_image);
        profile_Name = (TextView) findViewById(R.id.profile_DisplayName);
        total_Friends = (TextView) findViewById(R.id.profile_total);
        profile_Status = (TextView) findViewById(R.id.profile_DisplayStatus);
        sendRequest = (Button) findViewById(R.id.profile_requestBtn);
        declineRequest = (Button) findViewById(R.id.profile_DeclineBtn);

        mUserDatabaseDetail = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendReceiveDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        declineRequest.setVisibility(View.INVISIBLE);
        declineRequest.setEnabled(false);

        mUserDatabaseDetail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                profile_Name.setText(name);
                profile_Status.setText(status);
                if (!image.equals("default")) {
                    Picasso.with(ProfileActivity.this).load(image).into(pro_Image);
                }

                //use addListenerForSingleValueEvent beacause easier to use when return single value
                mFriendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("receive")) {
                                current_state = "req_received";
                                sendRequest.setText("Accept Friend Request");
                                declineRequest.setVisibility(View.VISIBLE);
                                declineRequest.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                current_state = "req_sent";
                                sendRequest.setText("Cancel Friend Request");
                                declineRequest.setVisibility(View.INVISIBLE);
                                declineRequest.setEnabled(false);
                            }
                            dialog.dismiss();

                        } else {
                            mFriendReceiveDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        current_state = "friends";
                                        sendRequest.setText("UnFriend this User");
                                        declineRequest.setVisibility(View.INVISIBLE);
                                        declineRequest.setEnabled(false);
                                    }
                                    dialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    dialog.dismiss();

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest.setEnabled(false);

                final String userCurrent_id = currentUser.getUid();

                ////////////////////current_state = not_friends//////////////////////
                if (current_state.equals("not_friends")) {

                    DatabaseReference newNotificationref = rootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put( currentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put( user_id + "/" + currentUser.getUid() + "/request_type", "receive");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mFriendReqDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            sendRequest.setEnabled(true);
                            current_state = "req_sent";
                            sendRequest.setText("Cancel Friend Request");
                            declineRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setEnabled(false);
                            Toast.makeText(ProfileActivity.this, "Successful to send Request", Toast.LENGTH_SHORT).show();
                        }
                    });


//                    mFriendReqDatabase.child(userCurrent_id).child(user_id).child("request_type")
//                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                mFriendReqDatabase.child(user_id).child(userCurrent_id).child("request_type").setValue("receive")
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                sendRequest.setEnabled(true);
//                                                current_state = "req_sent";
//                                                sendRequest.setText("Cancel Friend Request");
//                                                declineRequest.setVisibility(View.INVISIBLE);
//                                                declineRequest.setEnabled(false);
//                                                Toast.makeText(ProfileActivity.this, "Successful to send Request", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                            } else {
//                                Toast.makeText(ProfileActivity.this, "Failed to send Request", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                }
                ////////////////////req_sent//////////////////////
                if (current_state.equals("req_sent")) {
                    mFriendReqDatabase.child(userCurrent_id).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(userCurrent_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendRequest.setEnabled(true);
                                    current_state = "not_friends";
                                    sendRequest.setText("Send Friend Request");
                                    declineRequest.setVisibility(View.INVISIBLE);
                                    declineRequest.setEnabled(false);

                                }
                            });
                        }
                    });
                }
                ////////////////////////////////////////////////
                if (current_state.equals("req_received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put( "Friends/"+currentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put( "Friends/"+user_id + "/" + currentUser.getUid() + "/date", currentDate);
                    friendsMap.put( "Friend_request/"+currentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_request"+user_id + "/" + currentUser.getUid(), null);

                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    sendRequest.setEnabled(true);
                                    current_state = "friends";
                                    sendRequest.setText("UnFriend this User");
                                    declineRequest.setVisibility(View.INVISIBLE);
                                    declineRequest.setEnabled(false);

                        }
                    });

//                    mFriendReceiveDatabase.child(currentUser.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            mFriendReceiveDatabase.child(user_id).child(currentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    mFriendReqDatabase.child(userCurrent_id).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            mFriendReqDatabase.child(user_id).child(userCurrent_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    sendRequest.setEnabled(true);
//                                                    current_state = "friends";
//                                                    sendRequest.setText("UnFriend this User");
//                                                    declineRequest.setVisibility(View.INVISIBLE);
//                                                    declineRequest.setEnabled(false);
//
//                                                }
//                                            });
//                                        }
//                                    });
//                                }
//                            });
//
//                        }
//                    });
                }
                if (current_state.equals("friends")){

                    mFriendReceiveDatabase.child(userCurrent_id).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReceiveDatabase.child(user_id).child(userCurrent_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendRequest.setEnabled(true);
                                    current_state = "not_friends";
                                    sendRequest.setText("Send Friend Request");
                                    declineRequest.setVisibility(View.INVISIBLE);
                                    declineRequest.setEnabled(false);

                                }
                            });
                        }
                    });
                }
            }
        });

    }
}
