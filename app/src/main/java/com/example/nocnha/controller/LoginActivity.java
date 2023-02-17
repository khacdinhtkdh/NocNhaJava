package com.example.nocnha.controller;

import static com.example.nocnha.Constants.TOKEN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nocnha.MainActivity;
import com.example.nocnha.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    androidx.appcompat.widget.Toolbar toolbar_login;
    private FirebaseAuth mAuth;
    private DatabaseReference refUser;
    private FirebaseUser firebaseUser;
    String TAG = "KD_LOGIN";
    EditText edt_email, edt_password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        toolbar_login = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar_login);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(false);
        Button btnLoginRegister = findViewById(R.id.btnLoginRegister);
        Button btnLoginLogin = findViewById(R.id.btnLoginLogin);
        edt_email = findViewById(R.id.edtloginEmail);
        edt_password = findViewById(R.id.edtLoginPassword);

        mAuth = FirebaseAuth.getInstance();

        btnLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "register clicked");
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLoginLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "login clicked");
                String email = edt_email.getText().toString();
                String password = edt_password.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all information!", Toast.LENGTH_SHORT).show();
                } else
                    loginUser(email, password);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getToken();
                    Log.d(TAG, "signInWithEmail success");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Authentication failed: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "token: " + token);
                        sendRegistrationToServer(token);
                    }
                });
    }

    private void sendRegistrationToServer(String token) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String firebaseUserId = firebaseUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child(TOKEN).child(firebaseUserId).child("token");
            databaseReference.setValue(token);
        }
    }
}