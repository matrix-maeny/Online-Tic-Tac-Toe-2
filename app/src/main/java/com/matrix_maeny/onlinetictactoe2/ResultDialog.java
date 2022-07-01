package com.matrix_maeny.onlinetictactoe2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;

public class ResultDialog extends AppCompatDialogFragment {

    public static String win = "LOSE";

    private ResultDialogListener listener;
    AppCompatButton resultBtn;
    TextView resultTv;
    ImageView resultIv;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(themeWrapper);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.result_dialog, null);
        builder.setView(root);
        setCancelable(false);


        try {
            listener = (ResultDialogListener) requireContext();
        } catch (Exception e) {
            e.printStackTrace();
        }

        resultBtn = root.findViewById(R.id.gameResultBtn);
        resultTv = root.findViewById(R.id.gameResultTv);
        resultIv = root.findViewById(R.id.gameResultIv);

        if (win.equals("WON")) {
            root.setBackgroundColor(getResources().getColor(R.color.win_color));
            resultTv.setText(R.string.win_string);
            resultBtn.setText(R.string.play_again);
            resultIv.setImageResource(R.drawable.winner);

        } else if(win.equals("LOSE")) {
            root.setBackgroundColor(getResources().getColor(R.color.lose_color));
            resultTv.setText(R.string.lose_string);
            resultIv.setImageResource(R.drawable.lose);
            resultBtn.setVisibility(View.GONE);
        }else{
            root.setBackgroundColor(getResources().getColor(R.color.win_color));
            resultTv.setText(R.string.draw_match);
            if (MainActivity.host) {
                resultBtn.setVisibility(View.VISIBLE);
            }else{
                resultBtn.setVisibility(View.GONE);

            }
            resultIv.setImageResource(R.drawable.draw);

        }

        resultBtn.setOnClickListener(v -> {
            listener.restartGame();
            dismiss();
        });

        return builder.create();
    }

    public interface ResultDialogListener {
        void restartGame();

        void dialogBackPressed();
    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.dialogBackPressed();
    }
}
