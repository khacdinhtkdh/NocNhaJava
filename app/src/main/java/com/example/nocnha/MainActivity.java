package com.example.nocnha;

import static com.example.nocnha.Constants.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nocnha.adapter.RequestAdapter;
import com.example.nocnha.controller.AddFriendActivity;
import com.example.nocnha.controller.LoginActivity;
import com.example.nocnha.controller.SummaryActivity;
import com.example.nocnha.controller.UpdateInfoActivity;
import com.example.nocnha.databinding.ActivityMainBinding;
import com.example.nocnha.dialog.FilterDialog;
import com.example.nocnha.dialog.RequestDialog;
import com.example.nocnha.modelClass.Data;
import com.example.nocnha.modelClass.DataSend;
import com.example.nocnha.modelClass.Requests;
import com.example.nocnha.modelClass.RetrofitClient;
import com.example.nocnha.modelClass.UserInfo;
import com.example.nocnha.services.APIService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements RequestDialog.NoticeDialogListener, FilterDialog.FilterDialogListener {
    private final String TAG = "KD_MAIN";
    androidx.appcompat.widget.Toolbar toolbar_main;
    private RecyclerView recyclerViewList;
    FloatingActionButton fabRequest;

    private DatabaseReference refUser;
    String firebaseUserId;
    private UserInfo userInfo;
    ArrayList<Requests> listRequest, filterRequest;
    RequestAdapter requestAdapter;

    TextView txtUserName;
    ImageView imgProfile;
    String friendId;
    boolean isApprove;

    ValueEventListener valueEventListener;

    private String tokenApprove, tokenRequest;
    private APIService apiService;

    String[] permission = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        com.example.nocnha.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");
        setUpRecycleView();

        txtUserName = findViewById(R.id.txtMainUserName);
        imgProfile = findViewById(R.id.imgMainProfile);
        fabRequest = findViewById(R.id.addRequest);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        firebaseUserId = firebaseUser.getUid();
        refUser = FirebaseDatabase.getInstance().getReference().child(USERS).child(firebaseUser.getUid());

        filterRequest = new ArrayList<>();

        displayInformation();

        askNotificationPermission();
//        askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        apiService = RetrofitClient.getClient(FCM_URL).create(APIService.class);

        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRA_URL, userInfo.profile);
            startActivity(intent);
            finish();
        });

        fabRequest.setOnClickListener(view -> {
//                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            if (isApprove) {
                Toast.makeText(getApplicationContext(), "you are approver, not need request!", Toast.LENGTH_SHORT).show();
            } else {
                getTokenApprove();
                RequestDialog requestDialog = new RequestDialog();
                requestDialog.show(getSupportFragmentManager(), "");
            }
        });

        valueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    retrieveList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        refUser.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        refUser.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        refUser.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    private void retrieveList() {
        listRequest = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(REQUEST_LIST);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listRequest.clear();
                for (DataSnapshot p0 : snapshot.getChildren()) {
                    Requests request = p0.getValue(Requests.class);
                    assert request != null;
                    if ((Objects.equals(request.sender, firebaseUserId) && Objects.equals(request.receiver, friendId)) ||
                            (Objects.equals(request.sender, friendId) && Objects.equals(request.receiver, firebaseUserId))
                    ) {
                        listRequest.add(request);
                    }
                }
                requestAdapter = new RequestAdapter(listRequest);
                recyclerViewList.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private ArrayList<Integer> showAllRequest() {
        ArrayList<Integer> value = new ArrayList<>();
        int cntApprove = 0, priceApprove = 0;
        int cntReject = 0, priceReject = 0;
        int cntPending = 0, pricePending = 0;
        int cntAll = 0, priceAll = 0;
        for (Requests request : listRequest) {
            if (request.status == APPROVE) {
                cntApprove++;
                priceApprove += Integer.parseInt(request.price);
            } else if (request.status == REJECT) {
                cntReject++;
                priceReject += Integer.parseInt(request.price);
            } else if (request.status == PENDING) {
                cntPending++;
                pricePending += Integer.parseInt(request.price);
            }
            cntAll++;
            priceAll += Integer.parseInt(request.price);
        }
        value.add(cntApprove);
        value.add(cntReject);
        value.add(cntPending);
        value.add(cntAll);
        value.add(priceApprove);
        value.add(priceReject);
        value.add(pricePending);
        value.add(priceAll);
        return value;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_filter:
                FilterDialog filterDialog = new FilterDialog();
                filterDialog.show(getSupportFragmentManager(), "");
                break;
            case R.id.menu_addFriend:
                intent = new Intent(this, AddFriendActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_showSummary:

                ArrayList<Integer> value = showAllRequest();
                intent = new Intent(this, SummaryActivity.class);
                intent.putExtra(EXTRA_SUMMARY, value);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_signOut:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecycleView() {
        recyclerViewList = findViewById(R.id.listRequestMain);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Requests request = listRequest.get(pos);
                Integer action = direction == ItemTouchHelper.LEFT ? APPROVE : REJECT;
                doActionItem(request, action);

                Snackbar snackbar = Snackbar.make(viewHolder.itemView, "you are " +
                        (direction == ItemTouchHelper.RIGHT ? "rejected" : "approved") + ".", Snackbar.LENGTH_LONG);
                snackbar.setAction(android.R.string.cancel, view -> {
                    try {
                        doActionItem(request, PENDING);
                    } catch(Exception e) {
                        Log.e("MainActivity", e.getMessage());
                    }
                });
                snackbar.show();
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int pos = viewHolder.getAdapterPosition();
                Requests request = listRequest.get(pos);
                if (!Objects.equals(request.status, PENDING))
                    return 0;
                if (!isApprove)
                    return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.recycler_view_item_swipe_left_background))
                        .addSwipeLeftActionIcon(R.drawable.approve_24)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.recycler_view_item_swipe_right_background))
                        .addSwipeRightActionIcon(R.drawable.reject_24)
                        .addSwipeRightLabel(getString(R.string.action_reject))
                        .setSwipeRightLabelColor(Color.WHITE)
                        .addSwipeLeftLabel(getString(R.string.action_approve))
                        .setSwipeLeftLabelColor(Color.WHITE)
                        //.addCornerRadius(TypedValue.COMPLEX_UNIT_DIP, 16)
                        //.addPadding(TypedValue.COMPLEX_UNIT_DIP, 8, 16, 8)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewList);
    }

    private void displayInformation() {
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userInfo = snapshot.getValue(UserInfo.class);
                    assert userInfo != null;
                    txtUserName.setText(userInfo.userName);
                    friendId = userInfo.friend;
                    isApprove = Objects.equals(userInfo.type, "approve");
//                    Picasso.get().load(userInfo.profile).placeholder(R.drawable.ic_profile).into(imgProfile);
                    Glide.with(getApplicationContext()).load(userInfo.profile).override(32, 32).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void doActionItem(Requests request, Integer action) {
        request.status = action;
        FirebaseDatabase.getInstance().getReference().child(REQUEST_LIST)
                .child(request.ID).child("status").setValue(action);
        getTokenRequest(request.sender);
        String res = Objects.equals(action, APPROVE) ? "Approved" : "rejected";
        sendNotification(res, tokenRequest);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> sendNotification(res, tokenRequest), 500);

        retrieveList();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String catalog, String price, String reason) {
        Log.d(TAG, "ok clicked --> " + catalog + " " + price + " " + reason);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(REQUEST_LIST);
        String ID = databaseReference.push().getKey();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat cDate = new SimpleDateFormat("dd/MM/yyy");
        String curDate = cDate.format(new Date());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("catalog", catalog);
        hashMap.put("price", price);
        hashMap.put("reason", reason);
        hashMap.put("status", PENDING);
        hashMap.put("sender", firebaseUserId);
        hashMap.put("receiver", friendId);
        hashMap.put("date", curDate);
        hashMap.put("ID", ID);

        assert ID != null;
        databaseReference.child(ID).setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotification(catalog, tokenApprove);
                Toast.makeText(getApplicationContext(), "Sent request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void sendNotification(String body, String token){
        Log.d(TAG, body + " " + token);
        Data data = new Data(body);

        DataSend dataSend = new DataSend(data, token);
        apiService.sendNotification(dataSend).enqueue(new Callback<DataSend>() {
            @Override
            public void onResponse(@NonNull Call<DataSend> call, @NonNull Response<DataSend> response) {
                Log.d(TAG, "response.code() = " + response.code());
                if (response.code() == 200) {
                    Log.d(TAG, "sendNotification");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DataSend> call, @NonNull Throwable t) {
                Log.d(TAG, "send fail" + t);
            }
        });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted",Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                            Toast.LENGTH_LONG).show();
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }


    private void askPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getApplicationContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getTokenApprove(){
        if (friendId != null) {
            DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference()
                    .child(TOKEN).child(friendId).child("token");
            databaseReference.get().addOnCompleteListener(task -> {
                tokenApprove = task.getResult().getValue().toString();
                Log.d(TAG, tokenApprove);
            });
        }
    }

    private void getTokenRequest(String uid){
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference()
                .child(TOKEN).child(uid).child("token");
        databaseReference.get().addOnCompleteListener(task -> {
            tokenRequest = task.getResult().getValue().toString();
            Log.d(TAG, tokenRequest);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int status) {
        if (status < 3) {
            filterRequest.clear();
            for (Requests request : listRequest) {
                if (request.status == status) {
                    filterRequest.add(request);
                }
            }
            requestAdapter.setListRequest(filterRequest);
        } else {
            requestAdapter.setListRequest(listRequest);
        }
        requestAdapter.notifyDataSetChanged();
    }
}