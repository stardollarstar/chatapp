package com.badarch.groupchatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Loading extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedIns) {

        super.onCreate(savedIns);
        setContentView(R.layout.loading);

        TextView txt = findViewById(R.id.status_text);
        final Context cont = getApplicationContext();

        View circle = findViewById(R.id.circle);
        Animation a = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        a.setRepeatCount(-1);
        a.setDuration(1000);
        circle.startAnimation(a);

        Bundle extras = getIntent().getExtras();
        final String mail = Objects.requireNonNull(extras).getString("email");
        String password = extras.getString("password");
        final String name = extras.getString("Dname");
        int reqcode = extras.getInt("status", 0);

        final Intent toTheSignIn = new Intent(this, SignIn.class);
        final Intent toTheMain = new Intent(this, Welcoming.class);
        final Intent toTheSignUp = new Intent(this, SignUp.class);

        mAuth = FirebaseAuth.getInstance();

        if (reqcode == 0) {
            txt.setText("checking account!");
            mAuth.signInWithEmailAndPassword(mail, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startActivity(toTheMain);
                            } else {
                                setResult(5, toTheSignIn);
                            }
                            finish();
                        }
                    });
        }
        if(reqcode == 1) {
            txt.setText("Creating account!");
            mAuth.createUserWithEmailAndPassword(mail, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest pUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name).build();
                                user.updateProfile(pUpdate);


                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                Map<String, Object> mStoreUser = new HashMap<>();
                                mStoreUser.put("Name", name);
                                mStoreUser.put("Email", mail);
                                db.collection("users").document(user.getUid()).set(mStoreUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(cont, "Account created successfully", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                startActivity(toTheSignIn);
                                                finish();
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(cont, "Something went wrong", Toast.LENGTH_SHORT).show();
                                startActivity(toTheSignUp);
                            }
                        }
                    });
        }
    }
}
