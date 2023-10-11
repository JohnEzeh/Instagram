package com.example.instagram.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.MainActivity;
import com.example.instagram.R;
import com.example.instagram.fragments.ProfileFragment;
import com.example.instagram.model.User;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private Context mcontext;
    private List<User> musers;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mcontext, List<User> musers,boolean isFragment) {
        this.mcontext = mcontext;
        this.musers = musers;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_items, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final  User user = musers.get(position);

        viewHolder.btn_follow.setVisibility(View.VISIBLE);

        viewHolder.username.setText(user.getUsername());

        viewHolder.fullname.setText(user.getFullname());

        Glide.with(mcontext).load(user.getImageurl()).into(viewHolder.image_profile);

        isFollowing(user.getId(), viewHolder.btn_follow);

        //if it user image their will not be follow button because user can't follow the same user
        if (user.getId().equals(firebaseUser.getUid())){
            viewHolder.btn_follow.setVisibility(View.GONE);
        }

        //this will go to profile or account fragment
        viewHolder.itemView.setOnClickListener(view -> {

            if (isFragment) {

                SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", user.getId());
                editor.apply();

                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            } else {
                Intent intent = new Intent(mcontext, MainActivity.class);
                intent.putExtra("publisherid", user.getId());
                mcontext.startActivity(intent);
            }

        });

        //
        viewHolder.btn_follow.setOnClickListener(view -> {
            if (viewHolder.btn_follow.getText().toString().equals("follow")){

                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getId()).setValue(true);

                FirebaseDatabase.getInstance().getReference().child("follow").child(user.getId())
                        .child("followers").child(firebaseUser.getUid()).setValue(true);

                addNotifications(user.getId());

            } else {
                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("following").child(user.getId()).removeValue();

                FirebaseDatabase.getInstance().getReference().child("follow").child(user.getId())
                        .child("followers").child(firebaseUser.getUid()).removeValue();
            }
        });
    }


    private void addNotifications(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("userid", firebaseUser.getUid());
                hashMap.put("text", "started following you");
                hashMap.put("postid", "");
                hashMap.put("ispost", false);

                reference.push().setValue(hashMap);

            }


    @Override
    public int getItemCount() {
        return musers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.txtusername);
            fullname = itemView.findViewById(R.id.txtfullname);
            image_profile = itemView.findViewById(R.id.profile_image);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }

    }

    private void isFollowing(String userid, Button button){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).exists()){
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }//isFollowing method
}
