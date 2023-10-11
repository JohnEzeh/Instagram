package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
private Button registerbtn,loginbtn;

private FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //redirect if user is not null
        if (firebaseUser != null){
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        registerbtn = findViewById(R.id.registerbtn);
        loginbtn = findViewById(R.id.loginbtn);

        registerbtn.setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, RegisterActivity.class));
             finish();
        });

        loginbtn.setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        });
    }
}