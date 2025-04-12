package com.example.pamatek;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Manage_accounts extends Fragment {

    private LinearLayout layoutListAccounts, layoutCreateAccount;
    private Button btnListAccounts, btnCreateAccount;
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private List<Account> accountList;
    private DatabaseReference databaseRef;
    private EditText edtFirstName, edtMiddleInitial, edtLastName, edtUsername, edtPassword, edtConfirmPassword;
    private Button btnSubmitCreateAccount;

    private AlertDialog loadingDialog;

    private Spinner spinnerUserType;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_accounts, container, false);


        // Initialize views
        btnListAccounts = view.findViewById(R.id.btnListOfAccounts);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);
        layoutListAccounts = view.findViewById(R.id.layoutListAccounts);
        layoutCreateAccount = view.findViewById(R.id.layoutCreateAccount);
        recyclerView = view.findViewById(R.id.recyclerAccounts);
        edtFirstName = view.findViewById(R.id.edtFirstName);
        edtMiddleInitial = view.findViewById(R.id.edtMiddleInitial);
        edtLastName = view.findViewById(R.id.edtLastName);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnSubmitCreateAccount = view.findViewById(R.id.btnSubmitCreateAccount);





        String userType = UserPrefs.getUserType(getContext());
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        accountList = new ArrayList<>();
        adapter = new AccountAdapter(accountList, userType);
        recyclerView.setAdapter(adapter);

        // Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("Administrators");

        // Load data on first load
        loadAccountsFromFirebase();

        spinnerUserType = view.findViewById(R.id.spinnerUserType);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                Arrays.asList("admin", "super_admin")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);


        // Default visibility
        layoutListAccounts.setVisibility(View.VISIBLE);
        layoutCreateAccount.setVisibility(View.GONE);
        btnListAccounts.setBackgroundTintList(getResources().getColorStateList(R.color.green));
        btnCreateAccount.setBackgroundTintList(getResources().getColorStateList(R.color.gray));

        // Button click listeners
        btnListAccounts.setOnClickListener(v -> {
            layoutListAccounts.setVisibility(View.VISIBLE);
            layoutCreateAccount.setVisibility(View.GONE);

            btnListAccounts.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            btnCreateAccount.setBackgroundTintList(getResources().getColorStateList(R.color.gray));

            // Optional: refresh the list when switching back
            loadAccountsFromFirebase();
        });

        btnCreateAccount.setOnClickListener(v -> {
            layoutListAccounts.setVisibility(View.GONE);
            layoutCreateAccount.setVisibility(View.VISIBLE);

            btnCreateAccount.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            btnListAccounts.setBackgroundTintList(getResources().getColorStateList(R.color.gray));


        });





        btnSubmitCreateAccount.setOnClickListener(v -> {
            String firstName = edtFirstName.getText().toString().trim();
            String middleInitial = edtMiddleInitial.getText().toString().trim();
            String lastName = edtLastName.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();
            String userType_get = spinnerUserType.getSelectedItem().toString();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Pakiusap, punan po ang lahat ng patlang.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Hindi magkatugma ang mga password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username already exists
            databaseRef.orderByChild("username").equalTo(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(getContext(), "Mayroon nang umiiral na username. Pakiusap, pumili ng iba.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Username is unique — proceed to create account
                                String accountId = databaseRef.push().getKey();

                                Account newAccount = new Account(accountId, firstName, middleInitial, lastName, username, password, userType_get);

                                databaseRef.child(accountId).setValue(newAccount)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Matagumpay na nalikha ang account.", Toast.LENGTH_SHORT).show();
                                            clearCreateAccountFields();
                                            layoutCreateAccount.setVisibility(View.GONE);
                                            layoutListAccounts.setVisibility(View.VISIBLE);
                                            btnListAccounts.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                                            btnCreateAccount.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Nabigong makalikha ng account.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        // Get userType from UserPrefs
        String userType1 = UserPrefs.getUserType(getContext());

// Show toast to verify (optional)
        Toast.makeText(getContext(), "User type: " + userType1, Toast.LENGTH_SHORT).show();

// Hide or show the Create Account button based on userType
        if ("admin".equals(userType1)) {
            btnCreateAccount.setVisibility(View.GONE);
        } else if ("super_admin".equals(userType)) {
            btnCreateAccount.setVisibility(View.VISIBLE);
        }





        return view;
    }



    private void clearCreateAccountFields() {
        edtFirstName.setText("");
        edtMiddleInitial.setText("");
        edtLastName.setText("");
        edtUsername.setText("");
        edtPassword.setText("");
        edtConfirmPassword.setText("");
    }




    private void loadAccountsFromFirebase() {
        showLoadingDialog();  // Show loading dialog when fetching data

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountList.clear();
                for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
                    Account account = accountSnapshot.getValue(Account.class);
                    accountList.add(account);
                }
                adapter.notifyDataSetChanged();

                // Keep loading dialog visible for at least 5 seconds
                new android.os.Handler().postDelayed(() -> {
                    dismissLoadingDialog();  // Dismiss loading dialog after 5 seconds
                }, 5000); // 5000 milliseconds = 5 seconds
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissLoadingDialog();  // Dismiss loading dialog if an error occurs
                Toast.makeText(getContext(), "Nabigong ikarga ang mga datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
