package com.example.momento.authentication;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.example.momento.MainActivity;
import com.example.momento.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPass, etPass2;
    Button btnSignUp;
    TextView tvSignin;
    ProgressBar progressBar;

    //Authentication
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // set components
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etPass2 = findViewById(R.id.etPass2);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignin = findViewById(R.id.tvSignIn);
        progressBar = findViewById(R.id.progressSignup);

        fAuth = FirebaseAuth.getInstance();

        // returning user
        if (fAuth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get email & password
                String email = etEmail.getText().toString().trim();
                String password = etPass.getText().toString().trim();
                String password2 = etPass2.getText().toString().trim();

                if(!inputValidatin(email,password,password2)){
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                //Register the user
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(RegisterActivity.this, "Error ! :"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });


    }

    private boolean inputValidatin(String email, String password, String password2) {

        Boolean inputValid = true;
        if (TextUtils.isEmpty(email)){
            etEmail.setError("enter your email!");
            inputValid = false;
        }
        if (TextUtils.isEmpty(password)){
            etPass.setError("enter your password!");
            inputValid = false;
        }else if (password.length() < 6){
            etPass.setError("password must be minimum 6 characters long!");
            inputValid = false;
        }
        if (TextUtils.isEmpty(password2)){
            etPass2.setError("enter your password!");
            inputValid = false;
        } else if (!password.equals(password2)){
            etPass2.setError("Password does not match!");
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