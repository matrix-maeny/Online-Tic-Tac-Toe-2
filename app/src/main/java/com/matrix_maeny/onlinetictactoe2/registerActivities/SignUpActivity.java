package com.matrix_maeny.onlinetictactoe2.registerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.onlinetictactoe2.MainActivity;
import com.matrix_maeny.onlinetictactoe2.R;
import com.matrix_maeny.onlinetictactoe2.UserModel;
import com.matrix_maeny.onlinetictactoe2.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog dialog;

    private String username = null, email = null, password = null;
    private volatile boolean have = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        FirebaseApp.initializeApp(SignUpActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        initialize();

    }

    private void initialize() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Please wait few seconds");
        dialog.setTitle("Creating Account...");

        binding.signUpBtn.setOnClickListener(signUpBtnListener);
        binding.signUpLogin.setOnClickListener(signUpLoginBtnListener);
    }

    View.OnClickListener signUpBtnListener = v -> signUp();
    View.OnClickListener signUpLoginBtnListener = v -> {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    };

    private void signUp() {

        if (checkUsername() && checkEmail() && checkPassword()) {
            checkValidityOfName();
        }
    }

    private void checkValidityOfName() {
        have = false;
        dialog.show();

        firebaseDatabase.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot s : snapshot.getChildren()) {
                        UserModel model = s.getValue(UserModel.class);

                        if (model != null && model.getUsername().equals(username)) {
                            have = true;
                            break;
                        }
                    }
                }

                if (!have) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            UserModel model = new UserModel(username, email, password);
                            String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();

                            firebaseDatabase.getReference().child("Users").child(uid).setValue(model).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {

                                    Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Error: " + Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                            dialog.dismiss();
                        }

                    }).addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "Username not available", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }


    private boolean checkUsername() {
        try {
            username = Objects.requireNonNull(binding.signUpUsername.getText()).toString();
            if (!username.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();

        return false;
    }

    private boolean checkEmail() {
        try {
            email = Objects.requireNonNull(binding.signUpEmail.getText()).toString();
            if (!email.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkPassword() {
        try {
            password = Objects.requireNonNull(binding.signUpPassword.getText()).toString();
            if (!password.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onStart() {
        FirebaseApp.initializeApp(SignUpActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        super.onStart();


    }
}