package com.example.instagram.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.CommentActivity;
import com.example.instagram.FollowerActivity;
import com.example.instagram.R;
import com.example.instagram.fragments.PostDetailFragment;
import com.example.instagram.fragments.ProfileFragment;
import com.example.instagram.model.Post;
import com.example.instagram.model.User;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    public Context mcontext;
    public List<Post> mpost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mcontext, List<Post> mpost) {
        this.mcontext = mcontext;
        this.mpost = mpost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.post_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mpost.get(position);

        Glide.with(mcontext).load(post.getPostimage())
                .apply(new RequestOptions().placeholder(R.drawable.placeholderii)).into(holder.post_imager);

        if (post.getDescription().equals("")){
            holder.txt_description.setVisibility(View.GONE);
        } else {
            holder.txt_description.setVisibility(View.VISIBLE);
            holder.txt_description.setText(post.getDescription());
        }

        //WHERE WE I CALLED ALL MY METHODS
        publisherInformations(holder.image_profile, holder.post_username, holder.who_published, post.getPublisher());
        islikes(post.getPostid(), holder.is_like);
        numberOfLikes(holder.number_of_likes, post.getPostid());
        getComments(post.getPostid(), holder.txt_comment);
        isSaved(post.getPostid(), holder.saved);

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });

        holder.post_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });


        holder.who_published.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });

        holder.post_imager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();

            }
        });



        holder.saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.saved.getTag().equals("save")){

                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                } else {

                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });


        ///dsdvfsffs
        holder.number_of_likes.setOnClickListener(view -> {
            if (holder.is_like.getTag().equals("like")){
                FirebaseDatabase.getInstance().getReference().child("Likes")
                        .child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);

                addNotifications(post.getPublisher(), post.getPostid());

            } else {
                FirebaseDatabase.getInstance().getReference().child("Likes")
                        .child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
            }
        });

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mcontext, CommentActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("publisherid", post.getPublisher());
            mcontext.startActivity(intent);
        });


        holder.txt_comment.setOnClickListener(view -> {
            Intent intent = new Intent(mcontext, CommentActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("publisherid", post.getPublisher());
            mcontext.startActivity(intent);
        });

        holder.is_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, FollowerActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title","likes");
                mcontext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mpost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView post_imager,is_like,comment,saved;
        public CircleImageView image_profile;
        public TextView post_username,number_of_likes,who_published,txt_description,txt_comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_imager = itemView.findViewById(R.id.post_imager);
            is_like = itemView.findViewById(R.id.is_like);
            comment = itemView.findViewById(R.id.comment);
            saved = itemView.findViewById(R.id.saved);
            image_profile = itemView.findViewById(R.id.image_profile);
            post_username = itemView.findViewById(R.id.post_username);
            number_of_likes = itemView.findViewById(R.id.number_of_likes);
            who_published = itemView.findViewById(R.id.who_published);
            txt_description = itemView.findViewById(R.id.txt_description);
            txt_comment = itemView.findViewById(R.id.txt_comment);
        }
    }


    private void isSaved(String postid, ImageView imageView){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()){

                    imageView.setImageResource(R.drawable.ic_saved_black);
                    imageView.setTag("saved");
                } else {

                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getComments(String postid, TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                comments.setText(String.format("View All %dcomments", snapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void islikes(String postid, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assert firebaseUser != null;
                if (snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_likeddd);
                    imageView.setTag("liked");

                } else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void addNotifications(String userid, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("userid", firebaseUser.getUid());
                hashMap.put("text", "liked your post");
                hashMap.put("postid", postid);
                hashMap.put("ispost", true);

                reference.push().setValue(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void numberOfLikes(TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(String.format("%d likes", snapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void publisherInformations(CircleImageView image_profile, TextView username, TextView publisher, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                Glide.with(mcontext).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
