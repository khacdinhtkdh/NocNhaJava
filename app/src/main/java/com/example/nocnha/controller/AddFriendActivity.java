package com.example.nocnha.controller;

import static com.example.nocnha.Constants.APPROVE;
import static com.example.nocnha.Constants.PENDING;
import static com.example.nocnha.Constants.REJECT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.nocnha.MainActivity;
import com.example.nocnha.R;
import com.example.nocnha.adapter.UserAdapter;
import com.example.nocnha.modelClass.Requests;
import com.example.nocnha.modelClass.Users;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AddFriendActivity extends AppCompatActivity {

    private UserAdapter userAdapter;
    private ArrayList<Users> listUser;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private String firebaseUserId;
    private String TAG = "KD_ADD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Toolbar toolbar_addFriend = findViewById(R.id.toolbar_addFriend);
        setSupportActionBar(toolbar_addFriend);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Add Friend");
        actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar_addFriend.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        firebaseUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setUpRecycleView();
        searchEditText = findViewById(R.id.edtAddFriendEmail);
        listUser = new ArrayList<>();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, s.toString());
                searchForUser(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setUpRecycleView(){
        recyclerView = findViewById(R.id.addFriendRecycleView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Users user = listUser.get(pos);
                doActionItem(user, direction);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                int pos = viewHolder.getAdapterPosition();
//                Requests request = listRequest.get(pos);
//                if (!Objects.equals(request.status, PENDING))
//                    return 0;
//                if (!isApprove)
//                    return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(AddFriendActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(AddFriendActivity.this, R.color.purple_200))
                        //.addSwipeLeftActionIcon(R.drawable.approve_24)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(AddFriendActivity.this, R.color.purple_200))
                        //.addSwipeRightActionIcon(R.drawable.reject_24)
                        .addSwipeRightLabel(getString(R.string.requester))
                        .setSwipeRightLabelColor(Color.WHITE)
                        .addSwipeLeftLabel(getString(R.string.approver))
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void doActionItem(Users user, Integer action) {
        String name = user.userName;
        if (action == ItemTouchHelper.LEFT) {
            updateStatus("approve", "request",firebaseUserId , user.uid);
        } else {
            updateStatus("request", "approve",firebaseUserId , user.uid);
        }

        Toast.makeText(this, "selected approve friend " + name, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void updateStatus(String type1, String type2, String firebaseUserId, String userAddId){
        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userAddId);
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", type1);
        hashMap.put("friend", firebaseUserId);
        refUser.updateChildren(hashMap);
        refUser = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUserId);
        hashMap.put("type", type2);
        hashMap.put("friend", userAddId);
        refUser.updateChildren(hashMap);
    }
    private void searchForUser(String str) {
        Query queryUser = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("email").startAt(str).endAt(str+"\uf8ff");

        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUser.clear();
                for (DataSnapshot p : snapshot.getChildren()) {
                    Users user = p.getValue(Users.class);
                    if (user != null) {
                        if (!Objects.equals(user.uid, firebaseUserId)) {
                            listUser.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(listUser);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}