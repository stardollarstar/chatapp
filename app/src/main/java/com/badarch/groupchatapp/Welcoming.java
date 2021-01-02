package com.badarch.groupchatapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Welcoming extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView txt;

    @SuppressLint("SetTextI18n")
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();

        if(currentuser == null) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
        }
        else {
            txt.setText("Hi, " + currentuser.getDisplayName());
        }
    }

    protected void onCreate(Bundle savedIn) {
        super.onCreate(savedIn);
        setContentView(R.layout.welcoming);

        mAuth = FirebaseAuth.getInstance();

        txt = findViewById(R.id.welcome_text);

        Animation a = new AlphaAnimation(0,1);
        a.setRepeatCount(0);
        a.setDuration(3000);
        txt.startAnimation(a);

        final Intent intent = new Intent(this, MainActivity.class);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                        finish();
                    }
                }
        ,3000);
    }
    @Override
    public void onBackPressed() {

    }
}
