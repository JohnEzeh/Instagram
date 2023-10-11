package com.example.instagram;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.instagram.fragments.HomeFragment;
import com.example.instagram.fragments.NotificationFragment;
import com.example.instagram.fragments.ProfileFragment;
import com.example.instagram.fragments.SearchFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment;
    int numberOfNotification = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_heart);
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(numberOfNotification);

        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()){

                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;

                case R.id.nav_search:
                    selectedFragment = new SearchFragment();
                    break;

                case R.id.nav_add:
                    selectedFragment = null;
                    startActivity(new Intent(MainActivity.this, PostActivity2.class));
                    break;

                case R.id.nav_heart:
                    selectedFragment = new NotificationFragment();
                    break;

                case R.id.nav_profile:
                    SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    editor.apply();
                    selectedFragment = new ProfileFragment();
                    break;

            }

            if (selectedFragment != null){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        }
    };

//    private void getNumberOfNotification(String postid, TextView comments){
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);
//        reference.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("DefaultLocale")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_heart);
//                badgeDrawable.setVisible(true);
//                badgeDrawable.setNumber(numberOfNotification);
//
//                comments.setText(String.format("View All %dcomments", snapshot.getChildrenCount()));
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
}