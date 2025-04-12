package com.example.pamatek;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    PamahiinFragment pamahiinFragment = new PamahiinFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    Pamahiin_add pamahiinAdd = new Pamahiin_add();
    Manage_accounts manage_accounts = new Manage_accounts();

    FloatingActionButton floatingicon, editPamahiin, manageAccount;
    boolean isFabOpen = false;  // Track FAB state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        floatingicon = findViewById(R.id.floatingicon);
        editPamahiin = findViewById(R.id.edit_pamahiin);
        manageAccount = findViewById(R.id.manage_account);

        // Initially set buttons to INVISIBLE
        editPamahiin.setVisibility(View.INVISIBLE);
        manageAccount.setVisibility(View.INVISIBLE);

        // Check login status
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            floatingicon.setVisibility(View.VISIBLE);
//            Toast.makeText(MainActivity.this, "LOGGED", Toast.LENGTH_SHORT).show();

        } else {
            floatingicon.setVisibility(View.GONE);
//            Toast.makeText(MainActivity.this, "NOT LOG", Toast.LENGTH_SHORT).show();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                    return true;
                } else if (id == R.id.pamahiin) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, pamahiinFragment).commit();
                    return true;
                } else if (id == R.id.profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
                    return true;
                }
                return false;
            }
        });

        floatingicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFabOpen) {
                    editPamahiin.setVisibility(View.INVISIBLE);
                    manageAccount.setVisibility(View.INVISIBLE);
                    isFabOpen = false;
                } else {
                    editPamahiin.setVisibility(View.VISIBLE);
                    manageAccount.setVisibility(View.VISIBLE);
                    isFabOpen = true;
                }
            }
        });

        // OnClickListener for Edit Pamahiin button
        editPamahiin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPamahiin.setBackgroundColor(getResources().getColor(android.R.color.white));
                manageAccount.setBackgroundColor(getResources().getColor(R.color.primary_color));


                getSupportFragmentManager().beginTransaction().replace(R.id.container, pamahiinAdd).commit();
                bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                    bottomNavigationView.getMenu().getItem(i).setChecked(false);
                }
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

            }
        });

        // OnClickListener for Manage Account button
        manageAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageAccount.setBackgroundColor(getResources().getColor(android.R.color.white));
                editPamahiin.setBackgroundColor(getResources().getColor(R.color.primary_color));

//                Toast.makeText(MainActivity.this, "Manage Account clicked", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, manage_accounts).commit();

                bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                    bottomNavigationView.getMenu().getItem(i).setChecked(false);
                }
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

            }
        });
    }
}
