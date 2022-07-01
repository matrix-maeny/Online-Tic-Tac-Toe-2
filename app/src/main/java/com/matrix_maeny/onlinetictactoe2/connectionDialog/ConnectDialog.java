package com.matrix_maeny.onlinetictactoe2.connectionDialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;

import com.matrix_maeny.onlinetictactoe2.MainActivity;
import com.matrix_maeny.onlinetictactoe2.R;

public class ConnectDialog extends AppCompatDialogFragment {

    private EditText enterUserNameEt;
    private AppCompatButton connectBtn;
    private TextView errorTv;
    public static String currentUsername_ForDialog = null;
    public static boolean connected = false;


    private ConnectDialogListener listener;


    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        View root = requireActivity().getLayoutInflater().inflate(R.layout.connect_dialog, null);
        builder.setView(root);

        enterUserNameEt = root.findViewById(R.id.dialogEnterUserNameEt);
        connectBtn = root.findViewById(R.id.dialogConnectBtn);
        errorTv = root.findViewById(R.id.dialogErrorTv);
        errorTv.setVisibility(View.INVISIBLE);

        if (connected) {
            connectBtn.setText("DISCONNECT");
            root.findViewById(R.id.dialogTextInputLayout).setVisibility(View.GONE);
            errorTv.setVisibility(View.GONE);
        } else {
            connectBtn.setText("CONNECT");
            root.findViewById(R.id.dialogTextInputLayout).setVisibility(View.VISIBLE);

        }

        try {
            listener = (ConnectDialogListener) requireContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectBtn.setOnClickListener(connectBtnListener);

        return builder.create();
    }

    View.OnClickListener connectBtnListener = v -> {

        if (!connected) {
            if (checkUsername()) {
                listener.establishConnections();
                dismiss();
            }
        } else {
            listener.destroyConnection();
            dismiss();
        }

    };

    @SuppressLint("SetTextI18n")
    private boolean checkUsername() {
        String tempUsername = "";

        try {
            tempUsername = enterUserNameEt.getText().toString().trim(); // getting data

            if (!tempUsername.equals("")) { // checking emptiness

                if (tempUsername.equals(currentUsername_ForDialog)) { // checking with current username
                    errorTv.setText("Can't connect to yourself");
                    errorTv.setVisibility(View.VISIBLE);
                    return false;
                }

                MainActivity.connectedUsername_FromDialog = tempUsername;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        errorTv.setText("Please enter valid Username");
        errorTv.setVisibility(View.VISIBLE);

        return false;

    }

    public interface ConnectDialogListener {
        void establishConnections();

        void destroyConnection(); // to destroy connection
    }
}
