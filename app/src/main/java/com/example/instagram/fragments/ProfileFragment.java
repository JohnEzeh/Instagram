
package com.example.instagram.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.EditProfileActivity;
import com.example.instagram.FollowerActivity;
import com.example.instagram.OptionActivity;
import com.example.instagram.R;
import com.example.instagram.adapter.MyPhotoAdapter;
import com.example.instagram.model.Post;
import com.example.instagram.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class ProfileFragment extends Fragment {

    ImageView profile_img,img_options;
    TextView posts,followers,following, profile_fullname, added_bio, profile_username;
    Button edit_profile;
    ImageButton my_photos, my_saved_photos;

    private List<String> mySaves;

    RecyclerView recyclerview_saves;
    MyPhotoAdapter myPhotoAdapter_Saves;
    List<Post> postList_saves;


    RecyclerView recyclerview_profile;
    MyPhotoAdapter myPhotoAdapter;
    List<Post> postList;


    FirebaseUser firebaseUser;
    String profileid;

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences preferences = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = preferences.getString("profileid", "none");

        profile_img = view.findViewById(R.id.profile_img);
        img_options = view.findViewById(R.id.img_options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        profile_fullname = view.findViewById(R.id.profile_fullname);
        added_bio = view.findViewById(R.id.added_bio);
        profile_username = view.findViewById(R.id.profile_username);
        edit_profile = view.findViewById(R.id.edit_profile);
        my_photos = view.findViewById(R.id.my_photos);
        my_saved_photos = view.findViewById(R.id.my_saved_photos);

        recyclerview_profile = view.findViewById(R.id.recyclerview_profile);
        recyclerview_profile.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerview_profile.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getContext(), postList);
        recyclerview_profile.setAdapter(myPhotoAdapter);

        recyclerview_saves = view.findViewById(R.id.recyclerview_saved);
        recyclerview_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new GridLayoutManager(getContext(), 3);
        recyclerview_saves.setLayoutManager(linearLayoutManager1);
        postList_saves = new ArrayList<>();
        myPhotoAdapter_Saves = new MyPhotoAdapter(getContext(), postList_saves);
        recyclerview_saves.setAdapter(myPhotoAdapter_Saves);

        recyclerview_profile.setVisibility(View.VISIBLE);
        recyclerview_saves.setVisibility(View.GONE);

        userInfor();
        getFollowers();
        getNoPost();
        myPhotos();
        mysaves();

        if (profileid.equals(firebaseUser.getUid())){
            edit_profile.setText("Edit Profile");
        } else {
            checkFollow();
            my_saved_photos.setVisibility(View.GONE);
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String btn = edit_profile.getText().toString();
                if (btn.equals("Edit Profile")){
                    //go to edit profile
                    startActivity(new Intent(getContext(), EditProfileActivity.class));

                } else if (btn.equals("follow")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications();

                } else if (btn.equals("following")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        img_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OptionActivity.class);
                startActivity(intent);
            }
        });

        my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerview_profile.setVisibility(View.VISIBLE);
                recyclerview_saves.setVisibility(View.GONE);
            }
        });

        my_saved_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerview_profile.setVisibility(View.GONE);
                recyclerview_saves.setVisibility(View.VISIBLE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowerActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title","followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowerActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title","following");
                startActivity(intent);
            }
        });


        return view;
    }


    private void addNotifications(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("userid", firebaseUser.getUid());
                hashMap.put("text", "started following you");
                hashMap.put("postid", "");
                hashMap.put("ispost", false);

                reference.push().setValue(hashMap);

            }

    private void userInfor(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null){
                    return;
                }
                User user = snapshot.getValue(User.class);
                Glide.with(getContext()).load(Objects.requireNonNull(user).getImageurl()).into(profile_img);
                profile_username.setText(user.getUsername());
                profile_fullname.setText(user.getFullname());
                added_bio.setText(user.getBio());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()){
                    edit_profile.setText("following");

                } else {
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNoPost(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    if (Objects.requireNonNull(post).getPublisher().equals(profileid)) {
                        i++;
                    }
                }
                posts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    if (Objects.requireNonNull(post).getPublisher().equals(profileid)){
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mysaves(){
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    mySaves.add(dataSnapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /* this will read soso things from soso place
    */
    private void readSaves(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saves.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);

                    for (String id : mySaves){
                        if (Objects.requireNonNull(post).getPostid().equals(id)){
                            postList_saves.add(post);
                        }
                    }
                }
                myPhotoAdapter_Saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}