package com.example.nocnha.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nocnha.R;

public class RequestDialog extends DialogFragment {
    String TAG = "KD_DIALOG";

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String catalog, String price, String reason);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("must implement NoticeDialogListener");
        }
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_request, null);
        EditText edtCatalog = view.findViewById(R.id.edtDialogCatalog);
        EditText edtPrice = view.findViewById(R.id.edtDialogPrice);
        EditText edtReason = view.findViewById(R.id.edtDialogReason);

        builder.setTitle("Create New Request");
        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String cata = edtCatalog.getText().toString();
                        String price = edtPrice.getText().toString();
                        String reason = edtReason.getText().toString();
                        if (cata.isEmpty() || price.isEmpty() || reason.isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all", Toast.LENGTH_SHORT).show();
                        } else {
                            listener.onDialogPositiveClick(RequestDialog.this, cata, price, reason);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(RequestDialog.this);
                    }
                });

        return builder.create();
    }
}
