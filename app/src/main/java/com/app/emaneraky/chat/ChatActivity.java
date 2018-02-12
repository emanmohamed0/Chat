package com.app.emaneraky.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.emaneraky.chat.Modul.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String chatUserChoice;
    private Toolbar chatToolbar;
    private DatabaseReference mDatabaseRef;
    private TextView titleView;
    private TextView lastseenView;
    private CircleImageView imageView;
    private FirebaseAuth mAuth;
    private String currentUser;
    private EditText enterMessage;
    private ImageButton imgBtnAdd, imgBtnSend, recordVoiceButton;
    RecyclerView recYmessageList;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    private static final int loadlimitToLast = 10, PIC = 1;
    private int currentPage = 1;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private StorageReference firebaseStorage;
    private ProgressDialog mDialog;

    MediaRecorder mRecorder;
    private String mFileName = null;
    private String Key_Admin = "kd10K46z4dPCCnoN3IHxetQGSUq1";
    //Audio Runtime Permissions
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String[] permissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};

    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToWriteAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) ChatActivity.super.finish();
        if (!permissionToWriteAccepted) ChatActivity.super.finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserChoice = getIntent().getStringExtra("user_id");
        String userNameChat = getIntent().getStringExtra("user_name");

        chatToolbar = (Toolbar) findViewById(R.id.chat_appBar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarWithImage = inflater.inflate(R.layout.chat_appbar, null);
        actionBar.setCustomView(actionbarWithImage);

        //item for chat
        imgBtnAdd = (ImageButton) findViewById(R.id.add_btn);
        imgBtnSend = (ImageButton) findViewById(R.id.send_btn);
        recordVoiceButton = (ImageButton) findViewById(R.id.recordVoiceButton);
        enterMessage = (EditText) findViewById(R.id.enterMessage_edt);
        recYmessageList = (RecyclerView) findViewById(R.id.message_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swip);
        recYmessageList.setHasFixedSize(true);
        mDialog = new ProgressDialog(this);
        linearLayoutManager = new LinearLayoutManager(this);
        recYmessageList.setLayoutManager(linearLayoutManager);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/recorded_audio.3gp";

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recYmessageList.setAdapter(messageAdapter);

        //---------custom Action bar item -------------------
        titleView = (TextView) findViewById(R.id.bar_displayname);
        lastseenView = (TextView) findViewById(R.id.bar_lastseen);
        imageView = (CircleImageView) findViewById(R.id.chatBarImage);
        titleView.setText(userNameChat);

        //FireBase init
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        loadMessage();

        mDatabaseRef.child("Users").child(chatUserChoice).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = dataSnapshot.child("image").getValue().toString();

                if (dataSnapshot.hasChild("online")) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    if (online.equals("true")) {
                        lastseenView.setText("Online");
                    } else {
                        GetTimeAgo getTimeAgo = new GetTimeAgo();
                        Long lasttime = Long.parseLong(online);
                        String lastSeen = getTimeAgo.getTimeAgo(lasttime, getApplicationContext());
                        lastseenView.setText(lastSeen);
                    }
                }
                if (!image.equals("default")) {
                    Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.com_facebook_profile_picture_blank_portrait).into(imageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child("Chat").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatUserChoice)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUser + "/" + chatUserChoice, chatAddMap);
                    chatUserMap.put("Chat/" + chatUserChoice + "/" + currentUser, chatAddMap);

                    mDatabaseRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        if (currentUser.equals(Key_Admin)) {
//            ///////////////////////////////Chat_Admin///////////////////////////////
//           Intent Alluser = new Intent(ChatActivity.this,AllUserActivity.class);
//           startActivity(Alluser);
//        }
        ////////////////////////////////////////////////////////////////////
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        imgBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallaryIntent, "Select Image"), PIC);
            }
        });

        recordVoiceButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecording();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording();
                }

                return false;
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                messageList.clear();
                loadMessage();
            }
        });
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            uploadAudio();
        }
    }

    private void uploadAudio() {
        mDialog.setTitle("Uploading audio");
        mDialog.setMessage("Please Waiting....");
        mDialog.show();
//
//        StorageReference filepath = firebaseStorage.child("audio").child("new_audio.3gp");
//        final Uri uri = Uri.fromFile(new File(mFileName));


        final String current_user_ref = "messages/" + currentUser + "/" + chatUserChoice;
        final String chat_user_ref = "messages/" + chatUserChoice + "/" + currentUser;

        DatabaseReference user_message_push = mDatabaseRef.child("messages")
                .child(currentUser).child(chatUserChoice).push();

        final String push_id = user_message_push.getKey();


        StorageReference filepath = firebaseStorage.child("audio").child(push_id + ".3gp");
        Uri uri = Uri.fromFile(new File(mFileName));
        filepath.putFile(uri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDialog.dismiss();

                final String download_url = taskSnapshot.getDownloadUrl().toString();

                Map messageMap = new HashMap();
                messageMap.put("message", download_url);
                messageMap.put("seen", false);
                messageMap.put("type", "voice");
                messageMap.put("time", ServerValue.TIMESTAMP);
                messageMap.put("from", currentUser);

                Map messageUserMap = new HashMap();
                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                enterMessage.setText("");
                mDatabaseRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {

                            Log.d("CHAT_LOG", databaseError.getMessage().toString());

                        }

                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + currentUser + "/" + chatUserChoice;
            final String chat_user_ref = "messages/" + chatUserChoice + "/" + currentUser;

            DatabaseReference user_message_push = mDatabaseRef.child("messages")
                    .child(currentUser).child(chatUserChoice).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = firebaseStorage.child("message_images").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    String download_url = taskSnapshot.getDownloadUrl().toString();

                    Map messageMap = new HashMap();
                    messageMap.put("message", download_url);
                    messageMap.put("seen", false);
                    messageMap.put("type", "image");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", currentUser);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    enterMessage.setText("");
                    mDatabaseRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }
            });

        }
    }

//    private void loadMoreMessages() {
//    DatabaseReference messageRef = mDatabaseRef.child("messages").child(currentUser).child(chatUserChoice);
//
//    Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
//
//        messageQuery.addChildEventListener(new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//
//            Message message = dataSnapshot.getValue(Message.class);
//            String messageKey = dataSnapshot.getKey();
//
//            if(!mPrevKey.equals(messageKey)){
//
//                messageList.add(itemPos++, message);
//
//            } else {
//
//                mPrevKey = mLastKey;
//
//            }
//
//
//            if(itemPos == 1) {
//
//                mLastKey = messageKey;
//
//            }
//
//
//            Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);
//
//            messageAdapter.notifyDataSetChanged();
//
//            mRefreshLayout.setRefreshing(false);
//
//            linearLayoutManager.scrollToPositionWithOffset(10, 0);
//
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//        }
//
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    });
//
//}
//
//    private void loadMessages() {
//
//        DatabaseReference messageRef = mDatabaseRef.child("messages").child(currentUser).child(chatUserChoice);
//
//        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
//
//
//        messageQuery.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                Message message = dataSnapshot.getValue(Message.class);
//
//                itemPos++;
//
//                if(itemPos == 1){
//
//                    String messageKey = dataSnapshot.getKey();
//
//                    mLastKey = messageKey;
//                    mPrevKey = messageKey;
//
//                }
//
//                messageList.add(message);
//                messageAdapter.notifyDataSetChanged();
//
//                rec.scrollToPosition(messageList.size() - 1);
//
//                mRefreshLayout.setRefreshing(false);
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//    }

    private void loadMessage() {
        DatabaseReference messageRef = mDatabaseRef.child("messages").child(currentUser).child(chatUserChoice);
        Query messageQuery = messageRef.limitToLast(currentPage * loadlimitToLast);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                //    recYmessageList.scrollToPosition(messageList.size() - 1); this scroll to bottom
                mRefreshLayout.setRefreshing(false);

                linearLayoutManager.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        String message = enterMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String currentUserRef = "messages/" + currentUser + "/" + chatUserChoice;
            String chatUserRef = "messages/" + chatUserChoice + "/" + currentUser;

            //create push key
            DatabaseReference user_Push = mDatabaseRef.child("messages")
                    .child(currentUser).child(chatUserChoice).push();
            String push_id = user_Push.getKey();

            Map messageMAp = new HashMap();
            messageMAp.put("message", message);
            messageMAp.put("send", false);
            messageMAp.put("type", "text");
            messageMAp.put("time", ServerValue.TIMESTAMP);
            messageMAp.put("from", currentUser);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + push_id, messageMAp);
            messageUserMap.put(chatUserRef + "/" + push_id, messageMAp);

            mDatabaseRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("Chat Log", databaseError.getMessage().toString());
                    }
                }
            });
            enterMessage.setText("");
        }
    }
}