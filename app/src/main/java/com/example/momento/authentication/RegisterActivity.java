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
import com.example.momento.main.NavMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPass, etPass2;
    private Button btnSignUp;
    private TextView tvSignin;
    private ProgressBar progressBar;

    //Authentication
    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // set components
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.et_Email);
        etPass = findViewById(R.id.et_Pass);
        etPass2 = findViewById(R.id.etPass2);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignin = findViewById(R.id.tv_SignUp);
        progressBar = findViewById(R.id.progressSignup);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // returning user
        if (fAuth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(), NavMainActivity.class));
            finish();
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get user input
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPass.getText().toString().trim();
                String password2 = etPass2.getText().toString().trim();

                if(!inputValidatin(name,email,password,password2)){
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                //Register the user
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //get user id
                            userID = fAuth.getCurrentUser().getUid();
                            //add user data to firestore
                            DocumentReference documentReference = db.collection("Users").document(userID);
                            //save user data as hash map
                            Map<String,Object> user = new HashMap<>();
                            user.put("name" , name);
                            user.put("email", email);
                            user.put("init", false);

                            //push to cloud
                            documentReference.set(user);

                            // start main activity
                            startActivity(new Intent(getApplicationContext(), NavMainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(RegisterActivity.this, "error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });



            }
        });

        tvSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });


    }

    private boolean inputValidatin(String name, String email, String password, String password2) {

        Boolean inputValid = true;
        if (TextUtils.isEmpty(name)){
            etName.setError("enter your name!");
            inputValid = false;
        }
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