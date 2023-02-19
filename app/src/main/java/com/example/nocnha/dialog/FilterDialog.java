package com.example.nocnha.dialog;

import static com.example.nocnha.Constants.APPROVE;
import static com.example.nocnha.Constants.PENDING;
import static com.example.nocnha.Constants.REJECT;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nocnha.R;

public class FilterDialog extends DialogFragment {
    public interface FilterDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int type);
    }

    FilterDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (FilterDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("must implement NoticeDialogListener");
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter, null);
        RadioGroup radioGroup = view.findViewById(R.id.rd_group);

        builder.setTitle("Choose request type");

        builder.setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    int rdID = radioGroup.getCheckedRadioButtonId();
                    int status = 3;
                    switch (rdID) {
                        case R.id.rd_approve:
                            status = APPROVE;
                            break;
                        case R.id.rd_reject:
                            status = REJECT;
                            break;
                        case R.id.rd_pending:
                            status = PENDING;
                            break;
                        default:
                            break;
                    }
                    listener.onDialogPositiveClick(FilterDialog.this, status);
                });

        return builder.create();
    }
}
