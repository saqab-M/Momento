package com.example.momento.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.momento.R;
import com.example.momento.main.NavMainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etPassword;
    TextView tvRegister;
    Button btnLogin;
    ProgressBar progressBar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail= findViewById(R.id.et_Email);
        etPassword = findViewById(R.id.et_Pass);
        tvRegister = findViewById(R.id.tv_SignUp);
        btnLogin = findViewById(R.id.btn_SignIn);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {

            // get text from input fields
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            //validate input
            if(!inputValidation(email,password)){
                return;
            }
            progressBar.setVisibility(View.VISIBLE);

            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                if (task.isSuccessful()){
                    startActivity(new Intent(getApplicationContext(), NavMainActivity.class));
                    finish();
                }else {
                    progressBar.setVisibility(View.INVISIBLE);
                }

            });

        });


        tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        });

    }

    private boolean inputValidation(String email, String password) {

        boolean inputValid = true;
        if(TextUtils.isEmpty(email)){
            etEmail.setError("enter your email!");
            inputValid = false;
        }
        if(TextUtils.isEmpty(password)){
            etPassword.setError("enter you password!");
            inputValid = false;
        }

        hideKeyboard();
        return inputValid;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}