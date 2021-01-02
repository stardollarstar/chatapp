package com.badarch.groupchatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    private TextView mail;
    private TextView pw1;
    private TextView pw2;
    private TextView dN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        Button createAccount = (Button) findViewById(R.id.sign_up_button);
        mail = (TextView) findViewById(R.id.sign_up_email);
        pw1 = (TextView) findViewById(R.id.sign_up_password);
        pw2 = (TextView) findViewById(R.id.sign_up_confirm_password);
        dN = (TextView) findViewById(R.id.sign_up_displayName);

        createAccount.setOnClickListener(this);
    }
    public boolean isPasswordIsValid(String pw1, String pw2) {
        Context cont = getApplicationContext();
        if(pw1.length() >= 8) {
            if (pw1.equals(pw2)) {
                return true;
            }
            else {
                Toast.makeText(cont, "Password is not matching", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(cont, "Password length must be higher than 8", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    public boolean isEmailIsValid(String mail) {
        Context cont = getApplicationContext();
        if(Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            return true;
        }
        else {
            Toast.makeText(cont, "Invalid e-mail", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    public void onClick(View v) {
        if(v.getId() == R.id.sign_up_button) {
            final Context cont = getApplicationContext();
            String email = mail.getText().toString();
            String password1 = pw1.getText().toString();
            String password2 = pw2.getText().toString();
            String name = dN.getText().toString();
            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password1) && !TextUtils.isEmpty(password2) && !TextUtils.isEmpty(name)) {
                if(name.length() >= 5) {
                    if (isEmailIsValid(email)) {
                        if (isPasswordIsValid(password1, password2)) {
                            Intent intent = new Intent(this, Loading.class);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password1);
                            intent.putExtra("Dname", name);
                            intent.putExtra("status", 1);
                            startActivity(intent);
                        }
                    }
                }
                else {
                    Toast.makeText(cont, "Display name should have at least 5 characters", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(cont, "Please fill all inputs", Toast.LENGTH_SHORT).show();
            }
        }
    }
}