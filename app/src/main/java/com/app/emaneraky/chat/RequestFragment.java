package com.app.emaneraky.chat;


import android.content.Context;
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
public class RequestFragment extends Fragment {
    private RecyclerView recRequestsList;
    private DatabaseReference fireRequestDatabase;
    private DatabaseReference mUserFriends;
    private FirebaseAuth auth;
    private String current_userID;
    private View mainView;
    static Context c;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_request, container, false);
        recRequestsList = (RecyclerView) mainView.findViewById(R.id.request_list);
        auth = FirebaseAuth.getInstance();
        current_userID = auth.getCurrentUser().getUid();
        fireRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(current_userID);
        mUserFriends = FirebaseDatabase.getInstance().getReference().child("Users");

        fireRequestDatabase.keepSynced(true); //offline capability
        mUserFriends.keepSynced(true); //offline capability

        recRequestsList.setHasFixedSize(true);
        recRequestsList.setHasFixedSize(true);
        recRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        c = getContext();
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, RequestsViewHolder> firebaseRecyclerAdapterRequests = new FirebaseRecyclerAdapter<Friends, RequestsViewHolder>(
                Friends.class,
                R.layout.user_item,
                RequestsViewHolder.class,
                fireRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder requestsViewHolder, Friends friends, int i) {
                final String list_UserId = getRef(i).getKey();
                mUserFriends.child(list_UserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String image = dataSnapshot.child("image").getValue().toString();
                        final String status = dataSnapshot.child("status").getValue().toString();

                        requestsViewHolder.setName(name);
                        requestsViewHolder.setImage(image);
                        requestsViewHolder.setStatus(status);
                        requestsViewHolder.hView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent openChatIntent = new Intent(getContext(), ProfileActivity.class);
                                openChatIntent.putExtra("user_id", list_UserId);
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
        recRequestsList.setAdapter(firebaseRecyclerAdapterRequests);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        View hView;
        TextView userName;
        CircleImageView userProfile;
        ImageView onlineIcone;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            hView = itemView;
        }
        public void setStatus(String status) {
            TextView userName = (TextView) hView.findViewById(R.id.user_status);
            userName.setText(status);
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


    }

}
