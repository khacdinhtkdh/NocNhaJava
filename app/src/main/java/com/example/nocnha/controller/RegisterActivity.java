package com.example.nocnha.controller;

import static com.example.nocnha.Constants.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nocnha.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
public class RegisterActivity extends AppCompatActivity {

    EditText edtIdName, edtEmailAddress, edtPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference refUser;
    String TAG = "KD_R";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar_register = findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar_register);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Register");
        actionBar.setDisplayHomeAsUpEnabled(true);

        edtIdName = findViewById(R.id.edtIdName);
        edtEmailAddress = findViewById(R.id.edtEmailAddress);
        edtPassword = findViewById(R.id.edtPassword);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        toolbar_register.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnRegisterRegister = findViewById(R.id.btnRegisterRegister);
        btnRegisterRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String userName = edtIdName.getText().toString();
        String email = edtEmailAddress.getText().toString();
        String password = edtPassword.getText().toString();

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all information!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, userName + " " + email + " " + password);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        String id = user.getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        refUser =  database.getReference().child("Users").child(id);
                        HashMap<String, Object> userHash = new HashMap<>();
                        userHash.put("uid", id);
                        userHash.put("userName", userName);
                        userHash.put("profile", PROFILE_TMP);
                        userHash.put("cover", COVER_TMP);
                        userHash.put("email", email.toLowerCase());

                        refUser.updateChildren(userHash).addOnCompleteListener( new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "update child");
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.w(TAG, task.getException().getMessage());
                                    Toast.makeText(getApplicationContext(),
                                            "Error Message: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure" + task.getException().getMessage());
                        Toast.makeText(getApplicationContext(), "Authentication failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}