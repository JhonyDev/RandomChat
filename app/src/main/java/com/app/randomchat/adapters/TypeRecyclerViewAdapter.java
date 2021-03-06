package com.app.randomchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.Message;
import com.app.randomchat.Pojo.Super;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.Pojo.UserConHistory;
import com.app.randomchat.R;
import com.app.randomchat.activities.ChatActivity;
import com.app.randomchat.activities.PreviewImageActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class TypeRecyclerViewAdapter extends RecyclerView.Adapter<TypeRecyclerViewHolder> implements Info {

    private static final String TAG = "TAG";
    Context context;
    List<Super> listInstances;
    int type;

    public TypeRecyclerViewAdapter(Context context, List<Super> listInstances, int type) {
        this.context = context;
        this.listInstances = listInstances;
        this.type = type;
    }

    @NonNull
    @Override
    public TypeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: TYPE: " + viewType);

        LayoutInflater li = LayoutInflater.from(context);
        View view;

        if (viewType == TYPE_SHOW_RIGHT)
            view = li.inflate(R.layout.item_message_sender, parent, false);
        else if (viewType == TYPE_SHOW_LEFT)
            view = li.inflate(R.layout.item_message, parent, false);
        else
            view = li.inflate(R.layout.rv_user_detail, parent, false);

        return new TypeRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TypeRecyclerViewHolder holder, int position) {
        if (type == TYPE_USER) {
            User user = (User) listInstances.get(position);
            String userName = user.getFirstName() + " " + user.getLastName();
            holder.tvUserName.setText(userName);
//            holder.ivUserProfile.setImageURI(user.getUrlToImage());
            holder.tvLastText.setVisibility(View.GONE);
            return;
        }

        if (type == TYPE_MESSAGE) {
            UserConHistory message = (UserConHistory) listInstances.get(position);
            holder.tvUserName.setVisibility(View.VISIBLE);
            holder.tvUserName.setTextColor(context.getColor(R.color.black));
            holder.tvUserName.setText(message.getUserName());
            holder.ivUserProfile.setImageURI(message.getUserImageUrl());

            holder.ivUserProfile.setOnClickListener(v -> {
                getImageUriAge(message.getTargetUserId());
            });

            holder.ibTouchField.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(KEY_TARGET_USER_ID, message.getTargetUserId());
                context.startActivity(intent);
            });
            holder.tvLastText.setText(message.getLastMessage());
            return;
        }

        if (type == TYPE_REC_MESSAGE) {
            Message message = (Message) listInstances.get(position);

            try {
                String string = message.getPhotoUrl();
                Log.i(TAG, "onBindViewHolder: " + string);
            } catch (Exception e) {
                holder.layout.setVisibility(View.INVISIBLE);
                holder.cardView.setVisibility(View.INVISIBLE);
                e.printStackTrace();
                Log.i(TAG, "onBindViewHolder: " + e);
                return;
            }

            boolean isPhoto = message.getPhotoUrl() != null;
            if (isPhoto) {
                holder.messageTextView.setVisibility(View.GONE);
                holder.cardView.setCardBackgroundColor(context.getColor(R.color.fui_transparent));
                Glide.with(holder.photoImageView.getContext())
                        .load(message.getPhotoUrl())
                        .into(holder.photoImageView);
            } else {
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.photoImageView.setVisibility(View.GONE);
                Log.i(TAG, "onBindViewHolder: message" + message.getText());
                holder.messageTextView.setText(message.getText());
            }
            holder.authorTextView.setText(message.getFromUserName());
        }
    }

    private void getImageUriAge(String userId) {
        FirebaseDatabase.getInstance().getReference(USERS).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Intent intent = new Intent(context, PreviewImageActivity.class);
                intent.putExtra(KEY_IMAGE, user.getUserImageUrl());
                intent.putExtra(KEY_AGE, user.getAge());
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public int getItemCount() {
        return listInstances.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (type == TYPE_REC_MESSAGE) {
            Message message = (Message) listInstances.get(position);
            if (message.isShowOnRight())
                return TYPE_SHOW_RIGHT;
            else
                return TYPE_SHOW_LEFT;
        }

        return 0;
    }

}
