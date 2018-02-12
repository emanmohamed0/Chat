package com.app.emaneraky.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.app.emaneraky.chat.Modul.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {
    RecyclerView mUserList;
    private DatabaseReference mUserDatabase;
    static Context c;
    Toolbar toolbar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true); //offline capability
        mAuth = FirebaseAuth.getInstance();


        toolbar = (Toolbar) findViewById(R.id.user_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All Users");

        mUserList = (RecyclerView) findViewById(R.id.mUserlist);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        c = AllUserActivity.this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<User, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.user_item,
                UserViewHolder.class,
                mUserDatabase) {
            @Override
            protected void populateViewHolder(UserViewHolder userViewHolder, final User user, int position) {
                userViewHolder.setName(user.getName());
                userViewHolder.setStatus(user.getStatus());
                userViewHolder.setImage(user.getImage());

                final String user_id = getRef(position).getKey();

                userViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mAuth.getCurrentUser().getUid().equals("kd10K46z4dPCCnoN3IHxetQGSUq1")) {
                            Intent openChatIntent = new Intent(AllUserActivity.this, ChatAdminActivity.class);
                            openChatIntent.putExtra("user_id", user_id);
                            openChatIntent.putExtra("user_name", user.getName());
                            startActivity(openChatIntent);
                        } else {
                            Intent profileIntent = new Intent(AllUserActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("user_id", user_id);
                            startActivity(profileIntent);
                        }
                    }
                });
            }
        };
        c = AllUserActivity.this;
        mUserList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView userName;
        TextView userStatus;
        CircleImageView userProfile;

        public UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            userName = (TextView) view.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setStatus(String status) {
            userStatus = (TextView) view.findViewById(R.id.user_status);
            userStatus.setText(status);
        }

        public void setImage(final String profile) {
            userProfile = (CircleImageView) view.findViewById(R.id.user_image);
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
