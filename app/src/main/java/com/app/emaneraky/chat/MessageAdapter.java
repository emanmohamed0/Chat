package com.app.emaneraky.chat;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.emaneraky.chat.Modul.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Message> messageList;
    FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        MessageViewHolder messageViewHolder = new MessageViewHolder(v);
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        String current_id = mAuth.getCurrentUser().getUid();
        final Message m = messageList.get(position);
        String from_id = m.getFrom();
        String message_type = m.getType();
        Long time = m.getTime();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_id);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                holder.messageDisplayName.setText(name);
                Picasso.with(holder.messageProfile.getContext()).load(image).placeholder(R.drawable.com_facebook_profile_picture_blank_portrait).into(holder.messageProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (from_id.equals(current_id)) {
            holder.messageText.setBackgroundResource(R.drawable.txt_shape);
            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);

        } else {
            holder.messageText.setBackgroundResource(R.drawable.txt_shape);
            holder.messageText.setTextColor(Color.WHITE);


        }
        if (message_type.equals("text")) {
            holder.messageText.setText(m.getMessage());
            holder.message_image_layout.setVisibility(View.INVISIBLE);
            holder.message_voice_layout.setVisibility(View.INVISIBLE);
        } else if (message_type.equals("image")) {
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.message_voice_layout.setVisibility(View.INVISIBLE);
            Picasso.with(holder.messageProfile.getContext()).load(m.getMessage()).placeholder(R.drawable.com_facebook_profile_picture_blank_portrait).into(holder.message_image_layout);
        } else {
//            holder.messageText.setText("play voices");
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.message_image_layout.setVisibility(View.INVISIBLE);
            holder.message_voice_layout.setVisibility(View.VISIBLE);
            holder.message_voice_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.playSound(m.getMessage());
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageDisplayName, messageText, messageTime;
        CircleImageView messageProfile;
        ImageView message_image_layout;
        ImageButton message_voice_layout;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageDisplayName = (TextView) itemView.findViewById(R.id.message_displayname);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            messageTime = (TextView) itemView.findViewById(R.id.message_text);
            message_image_layout = (ImageView) itemView.findViewById(R.id.message_image_layout);
            messageProfile = (CircleImageView) itemView.findViewById(R.id.message_profile);
            message_voice_layout = (ImageButton)itemView.findViewById(R.id.message_voice_layout);
        }

        //If voice message add them to Firebase.Storage
        public void addVoiceToMessages(String url) {
            try {
                MediaPlayer player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(url);
                player.prepare();
                player.start();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        private void playSound(String uri) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(uri);
            } catch (Exception e) {

            }
            mediaPlayer.prepareAsync();
            //You can show progress dialog here untill it prepared to play
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //Now dismis progress dialog, Media palyer will start playing
                    mp.start();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // dissmiss progress bar here. It will come here when MediaPlayer
                    //  is not able to play file. You can show error message to user
                    return false;
                }
            });
        }
    }
}
