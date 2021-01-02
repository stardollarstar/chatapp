package com.badarch.groupchatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    protected void onCreate(Bundle savedOnI) {
        super.onCreate(savedOnI);
        setContentView(R.layout.sign_in);
        Button butt = (Button) findViewById(R.id.signin);
        Button sUp = (Button) findViewById(R.id.signup);
        mAuth = FirebaseAuth.getInstance();
        butt.setOnClickListener(this);
        sUp.setOnClickListener(this);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin: {
                EditText username = (EditText) findViewById(R.id.username);
                EditText password = (EditText) findViewById(R.id.password);
                String UN = username.getText().toString();
                String PW = password.getText().toString();
                Intent intent2 = new Intent(this, Loading.class);
                if(!TextUtils.isEmpty(UN) && !TextUtils.isEmpty(PW)) {
                    intent2.putExtra("email", UN);
                    intent2.putExtra("password", PW);
                    intent2.putExtra("status", 0);
                    startActivityForResult(intent2, 1);
                }
                else {
                    Context cont = getApplicationContext();
                    Toast.makeText(cont, "Please fill both input", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.signup: {
                Intent intent3 = new Intent(this, SignUp.class);
                startActivity(intent3);
            }
        }
    }
    protected void onActivityResult(int reqcode, int rescode, @Nullable Intent intent) {
        super.onActivityResult(reqcode, rescode, intent);
            if(rescode == 5) {
                Context cont = getApplicationContext();
                Toast.makeText(cont, "Wrong username or password", Toast.LENGTH_SHORT).show();
            }
    }
}
