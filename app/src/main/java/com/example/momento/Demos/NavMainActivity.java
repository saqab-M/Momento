package com.example.momento.Demos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.momento.HomeFragment;
import com.example.momento.R;
import com.example.momento.SettingsFragment;
import com.google.android.material.navigation.NavigationView;

public class NavMainActivity extends AppCompatActivity {

    Toolbar toolbar;
    NavigationView navView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_main);

        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawerLayout);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new HomeFragment()).commit();

        // set toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.home_item) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new HomeFragment()).commit();
                } else if (item.getItemId() == R.id.profile_item) {
                    Toast.makeText(NavMainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.settings_item) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new SettingsFragment()).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);


                return false;
            }
        });



    }
}