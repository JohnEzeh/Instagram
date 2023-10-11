package com.example.instagram.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ServiceCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.adapter.PostAdapter;
import com.example.instagram.adapter.StoryAdapter;
import com.example.instagram.model.Post;
import com.example.instagram.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {
    private PostAdapter postAdapter;
private List<Post> postList;

private List<String> followingList;

ProgressBar progressBar;

private RecyclerView recyclerView_story;
private StoryAdapter storyAdapter;
private List<Story> storyList;


    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_vine);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        //STORY: initialisations
        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager(linearLayoutManager1);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);



        progressBar = view.findViewById(R.id.progress_spinnax);

        checkFollowing();

        return view;
    }

   private void checkFollowing(){
        followingList = new ArrayList<>();
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                 .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                 .child("following");

         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 followingList.clear();
                 for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                     followingList.add(dataSnapshot.getKey());

                 }
                 readPost();
                 readStory();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
   }


   ///////////if you follow another user that only when you can see their activities///////////////////
       private void readPost(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                     for (String id : followingList){
                         assert post != null;
                         if (post.getPublisher().equals(id)){
                             postList.add(post);
                         }
                     }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }/////////////////////////////////////////////////////END OF READ POST////////////////////////////////////////////////

    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timeCurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));
                for (String id: followingList){
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot dataSnapshot : snapshot.child(id).getChildren()){
                        story = dataSnapshot.getValue(Story.class);
                        if (timeCurrent > story.getTimestart() && timeCurrent < story.getTimeend()){
                            countStory++;
                        }
                    }

                    if (countStory > 0){
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}