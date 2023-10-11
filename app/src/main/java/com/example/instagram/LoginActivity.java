package com.example.instagram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText emaillog,passwordlog;
    private Button login;
    private TextView txt_signup;

    private FirebaseAuth auth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emaillog = findViewById(R.id.emaillog);
        passwordlog = findViewById(R.id.passwordlog);
        login = findViewById(R.id.login);
        txt_signup = findViewById(R.id.txt_signup);

        auth = FirebaseAuth.getInstance();


        txt_signup.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            //finish();
        });

        login.setOnClickListener(view -> {
            pd = new ProgressDialog(LoginActivity.this);
            pd.setMessage("please wait.......");
            pd.show();

            String str_email = emaillog.getText().toString().trim();
            String str_password = passwordlog.getText().toString().trim();

            if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
                Toast.makeText(this, "all fields are required!!", Toast.LENGTH_SHORT).show();
            } else {

                auth.signInWithEmailAndPassword(str_email,str_password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid());

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pd.dismiss();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                  pd.dismiss();
                                }
                            });


                        } else {

                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}