package com.example.nocnha.adapter;

import static com.example.nocnha.Constants.APPROVE;
import static com.example.nocnha.Constants.REJECT;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nocnha.R;
import com.example.nocnha.modelClass.Requests;

import java.util.ArrayList;
import java.util.Objects;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Requests> listRequest;

    public RequestAdapter(ArrayList<Requests> listRequest) {
        this.listRequest = listRequest;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_request_expand, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Requests request = listRequest.get(position);
        ViewHolder mHolder = (ViewHolder) holder;
        mHolder.catalog.setText(request.catalog);
        if (Objects.equals(request.status, APPROVE)) {
            mHolder.imgStatus.setImageResource(R.drawable.approve_24_2);
        } else if (Objects.equals(request.status, REJECT)) {
            mHolder.imgStatus.setImageResource(R.drawable.reject_24_2);
        } else mHolder.imgStatus.setImageResource(R.drawable.baseline_access_time_24);

        mHolder.price.setText(request.price);
        mHolder.reason.setText(request.reason);
        mHolder.date.setText(request.date);

    }

    @Override
    public int getItemCount() {
        return listRequest.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView catalog;
        TextView price, reason, date;
        ImageView imgStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStatus = itemView.findViewById(R.id.imgListRequestStatus);
            catalog = itemView.findViewById(R.id.txtListCatalogName);
            price = itemView.findViewById(R.id.list_request_detail_price);
            reason = itemView.findViewById(R.id.list_request_detail_reason);
            date = itemView.findViewById(R.id.list_request_detail_date);
        }
    }
}
