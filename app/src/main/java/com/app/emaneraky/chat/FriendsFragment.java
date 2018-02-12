package com.app.emaneraky.chat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView recFriendsList;
    private DatabaseReference fireFriendsDatabase;
    private DatabaseReference mUserFriends;
    private FirebaseAuth auth;
    private String current_userID;
    private View mainView;
    static Context c;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        recFriendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        auth = FirebaseAuth.getInstance();
        current_userID = auth.getCurrentUser().getUid();
        fireFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_userID);
        mUserFriends = FirebaseDatabase.getInstance().getReference().child("Users");

        fireFriendsDatabase.keepSynced(true); //offline capability
        mUserFriends.keepSynced(true); //offline capability

        recFriendsList.setHasFixedSize(true);
        recFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        c = getContext();
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        c = getContext();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsViewHolderFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.user_item,
                FriendsViewHolder.class,
                fireFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {
                friendsViewHolder.setDate(friends.getDate());

              final String  list_UserID = getRef(position).getKey();
                mUserFriends.child(list_UserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String image = dataSnapshot.child("image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            String value = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(value);
                        }

                        friendsViewHolder.setName(name);
                        friendsViewHolder.setImage(image);
                        friendsViewHolder.hView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence option[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("select Option");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position) {
                                        //click on single item in dialog
                                        if (position == 0){
                                            Intent openProfile = new Intent(c, ProfileActivity.class);
                                            openProfile.putExtra("user_id", list_UserID);
                                            startActivity(openProfile);
                                        } if (position == 1) {
                                            Intent chatIntent = new Intent(c, ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_UserID);
                                            chatIntent.putExtra("user_name", name);

                                            startActivity(chatIntent);

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        recFriendsList.setAdapter(friendsViewHolderFirebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View hView;
        TextView userName;
        CircleImageView userProfile;
        ImageView onlineIcone;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            hView = itemView;
        }

        public void setDate(String date) {
            TextView userName = (TextView) hView.findViewById(R.id.user_status);
            userName.setText(date);
        }

        public void setName(String name) {
            userName = (TextView) hView.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setImage(final String profile) {
            userProfile = (CircleImageView) hView.findViewById(R.id.user_image);
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

        public void setUserOnline(final String online) {
            onlineIcone = (ImageView) hView.findViewById(R.id.user_online);
            if (online.equals("true")) {
                onlineIcone.setVisibility(View.VISIBLE);
            } else {
                onlineIcone.setVisibility(View.INVISIBLE);

            }

        }
    }
}
