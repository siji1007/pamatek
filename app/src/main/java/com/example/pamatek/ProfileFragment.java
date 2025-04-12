package com.example.pamatek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ProfileFragment extends Fragment {




    private Button btnLoginAdmin;
    private ImageView btnAbout;
    private TextView tvUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find Login Button
        btnLoginAdmin = view.findViewById(R.id.btnLoginAdmin);

        tvUsername = view.findViewById(R.id.tvUsername); // ✅ Reference the username TextView
        btnAbout = view.findViewById(R.id.ivAbout);

        // Show saved username if logged in
        displayUsername();

        updateLoginButtonText();

        // Handle Login/Logout Click
        btnLoginAdmin.setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                // Perform Logout
                logoutUser();
            } else {
                // Open Login Dialog
                FragmentManager fragmentManager = getParentFragmentManager();
                LoginDialogFragment loginDialog = new LoginDialogFragment();
                loginDialog.show(fragmentManager, "login_dialog");
            }
        });

        btnAbout.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            AboutDialogFragment aboutDialog = new AboutDialogFragment();
            aboutDialog.show(fragmentManager, "about_dialog");
        });

        return view;
    }

    private void displayUsername() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
            tvUsername.setVisibility(View.VISIBLE); // Show it

            // ✅ Show a toast with the username
//            Toast.makeText(getContext(), "Logged in as: " + username, Toast.LENGTH_SHORT).show();

        } else {
            tvUsername.setText("");
            tvUsername.setVisibility(View.GONE); // Hide it if no username
        }
    }



    // Method to check login status
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }





    private void updateLoginButtonText() {
        if (isUserLoggedIn()) {
            btnLoginAdmin.setText("LUMABAS");
        } else {
            btnLoginAdmin.setText("Pumasok bilang Tagapangasiwa");
        }
    }


    private void logoutUser() {
        // Access shared preferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Log out: clear preferences
        editor.putBoolean("isLoggedIn", false);
        editor.remove("username"); // Remove the saved username
        editor.apply();

        // Optionally, update login button text (if needed)
        updateLoginButtonText();

        // Add a delay before launching the SplashScreen
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent to start the SplashScreen activity
                Intent intent = new Intent(requireActivity(), SplashScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Ensure new task and clear the current activity stack
                startActivity(intent);

                // Optionally, finish the current activity to prevent it from being part of the back stack
                requireActivity().finish();
            }
        }, 100); // 100 ms delay before starting SplashScreen
    }





    @Override
    public void onResume() {
        super.onResume();
        updateLoginButtonText();
        displayUsername(); // ✅ refresh username when returning
    }
}

