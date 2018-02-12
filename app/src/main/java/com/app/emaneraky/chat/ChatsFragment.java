package com.app.emaneraky.chat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.emaneraky.chat.Modul.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView recChatList;
    private View view;
    private FirebaseAuth auth;
    private String current_User;
    static Context c;

    private DatabaseReference mChatRefData;
    private DatabaseReference mUserRefData;
    private DatabaseReference mMessageData;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats, container, false);
        recChatList = (RecyclerView) view.findViewById(R.id.chat_list);
        recChatList.setHasFixedSize(true);
        recChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        current_User = auth.getCurrentUser().getUid();
        mChatRefData = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_User);
        mUserRefData = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageData = FirebaseDatabase.getInstance().getReference().child("messages").child(current_User);
        c = getContext();
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, ChatViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, ChatViewHolder>(
                Friends.class,
                R.layout.user_item,
                ChatViewHolder.class,
                mChatRefData
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder chatViewHolder, Friends friends, int position) {

                final String list_UserID = getRef(position).getKey();
                Query lastMessageQuery = mMessageData.child(list_UserID).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        String voice = dataSnapshot.child("type").getValue().toString();
//                        chatViewHolder.setMessage(data, friends.isSeen());
                        if (voice.equals("voice")) {
                            chatViewHolder.setLastMessage("voice sent");
                        } else {
                            chatViewHolder.setLastMessage(data);
                        }
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

                mUserRefData.child(list_UserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String image = dataSnapshot.child("image").getValue().toString();

                        chatViewHolder.setName(name);
                        chatViewHolder.setImage(image);
                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            chatViewHolder.setUserOnline(userOnline);

                        }
                        chatViewHolder.holderView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent openChatIntent = new Intent(getContext(), ChatActivity.class);
                                openChatIntent.putExtra("user_id", list_UserID);
                                openChatIntent.putExtra("user_name", name);
                                startActivity(openChatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        recChatList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View holderView;
        TextView userName;
        CircleImageView userProfile;
        TextView lastMessage;
        ImageView onlineIcone;

        public ChatViewHolder(View itemView) {
            super(itemView);
            holderView = itemView;
        }

        public void setLastMessage(String lastmessage) {
            lastMessage = (TextView) holderView.findViewById(R.id.user_status);
            lastMessage.setText(lastmessage);

//            if(!isSeen){
//                lastMessage.setTypeface(lastMessage.getTypeface(), Typeface.BOLD);
//            } else {
//                lastMessage.setTypeface(lastMessage.getTypeface(), Typeface.NORMAL);
//            }

        }

        public void setName(String name) {
            userName = (TextView) holderView.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setImage(final String profile) {
            userProfile = (CircleImageView) holderView.findViewById(R.id.user_image);
//            Picasso.with(c).load(profile).placeholder(R.drawable.com_facebook_profile_picture_blank_portrait).into(userProfile);

            Picasso.with(c).load(profile).networkPolicy(NetworkPolicy.OFFLINE).into(userProfile, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    //in case online
                    Picasso.with(c).load(profile).placeholder(R.drawable.com_facebook_profile_picture_blank_portrait).into(userProfile);

                }
            });
        }

        public void setUserOnline(String online_status) {

            onlineIcone = (ImageView) holderView.findViewById(R.id.user_online);

            if (online_status.equals("true")) {

                onlineIcone.setVisibility(View.VISIBLE);
            } else {
                onlineIcone.setVisibility(View.INVISIBLE);

            }

        }
    }
}
