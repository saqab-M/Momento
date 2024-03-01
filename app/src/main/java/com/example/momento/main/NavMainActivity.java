package com.example.momento.main;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.momento.R;
import com.example.momento.authentication.LoginActivity;
import com.example.momento.authentication.UserInitActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class NavMainActivity extends AppCompatActivity {

    Toolbar toolbar;
    NavigationView navView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;

    //nav header components
    TextView uName;
    TextView uEmail;

    //fire base
    FirebaseAuth fAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_main);

        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawerLayout);
        View header = navView.getHeaderView(0);

        // set header components
        uName = header.findViewById(R.id.txt_username);
        uEmail = header.findViewById(R.id.txt_email);

        //uName.setText("jacob");

        //set firebase
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new HomeFragment()).commit();

        // set toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // init check and set user info
        setUser();

        navView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home_item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new HomeFragment()).commit();
            } else if (item.getItemId() == R.id.profile_item) {
                Toast.makeText(NavMainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.settings_item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new SettingsFragment()).commit();
            } else if (item.getItemId() == R.id.logout_item) {
                //TODO: implement a conformation pop up for logout
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);


            return false;
        });



    }

    private void setUser() {

        //TODO set name size limit/username at register
        DocumentReference docRef = db.collection("Users").document(Objects.requireNonNull(fAuth.getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();

                //init check
                if(doc.contains("init") && Boolean.TRUE.equals(doc.getBoolean("init"))){
                    // user is initialized
                }else{
                    startActivity(new Intent(getApplicationContext(), UserInitActivity.class));
                    finish();
                }

                //set user info
                if (doc.contains("name")){
                    String name = doc.getString("name");
                    uName.setText(name);
                }
                if (doc.contains("email")){
                    String email = doc.getString("email");
                    uEmail.setText(email);
                }

            }


        });

    }
}