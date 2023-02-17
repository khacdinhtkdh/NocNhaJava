package com.example.nocnha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nocnha.R;
import com.example.nocnha.modelClass.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    ArrayList<Users> listUser;
    public UserAdapter(ArrayList<Users> users) {
        this.listUser = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = listUser.get(position);
        holder.userName.setText(user.userName);
        Picasso.get().load(user.profile).placeholder(R.drawable.ic_profile).into(holder.profile);
        holder.email.setText(user.email);
    }

    @Override
    public int getItemCount() {
        return listUser.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView email;
        ImageView profile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.imgSearchProfile);
            userName = itemView.findViewById(R.id.txtSearchUserName);
            email = itemView.findViewById(R.id.txtSearchEmail);
        }
    }
}
