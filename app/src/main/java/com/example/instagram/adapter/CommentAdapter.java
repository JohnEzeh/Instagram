package com.example.instagram.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.MainActivity;
import com.example.instagram.R;
import com.example.instagram.model.Comment;
import com.example.instagram.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
 private final Context mContext;
 private final List<Comment> mComment;

 private FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, List<Comment> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comments_items, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Comment comment = mComment.get(position);

        holder.txt_commented.setText(comment.getComments());

        getUserInfor(holder.commenter_image_profile, holder.comment_txt_username, comment.getPublisher());

        holder.txt_commented.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("publisherid", comment.getPublisher());
            mContext.startActivity(intent);
        });

        holder.commenter_image_profile.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("publisherid", comment.getPublisher());
            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

    public CircleImageView commenter_image_profile;
    public TextView comment_txt_username,txt_commented;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        commenter_image_profile = itemView.findViewById(R.id.commenter_image_profile);
        comment_txt_username = itemView.findViewById(R.id.comment_txt_username);
        txt_commented = itemView.findViewById(R.id.txt_commented);

     }
  }

  private void getUserInfor(ImageView imageView, TextView username, String publisherid){
      DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherid);

      reference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              User user = snapshot.getValue(User.class);
              assert user != null;
              Glide.with(mContext).load(user.getImageurl()).into(imageView);
              username.setText(user.getUsername());

          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      });
    }

}
