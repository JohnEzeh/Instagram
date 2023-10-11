package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.adapter.CommentAdapter;
import com.example.instagram.model.Comment;
import com.example.instagram.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    EditText addcomment;
    CircleImageView userImageProfile;
    TextView post;

    String postid;
    String publisherid;

    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //TOOLBAR STUFF
        Toolbar toolbar = findViewById(R.id.another_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });//END OF TOOLBAR STUFF

        recyclerView = findViewById(R.id.recycler_comment);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this,commentList);
        recyclerView.setAdapter(commentAdapter);


        addcomment = findViewById(R.id.add_comment);
        userImageProfile = findViewById(R.id.user_image_profile);
        post = findViewById(R.id.post_comment);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        post.setOnClickListener(view -> {
            if (addcomment.getText().toString().equals("")){
                Toast.makeText(CommentActivity.this, "you can't post empty comment", Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });

        getImage();
        readComments();

    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comments", addcomment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());
        reference.push().setValue(hashMap);
        addNotifications();
        addcomment.setText("");
    }

    private void addNotifications(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("userid", firebaseUser.getUid());
                hashMap.put("text", "commented:"+addcomment.getText().toString());
                hashMap.put("postid", postid);
                hashMap.put("ispost", true);

                reference.push().setValue(hashMap);

            }

    private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(userImageProfile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}