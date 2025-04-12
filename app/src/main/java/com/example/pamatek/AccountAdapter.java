package com.example.pamatek;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList;

    private String userType;


    public AccountAdapter(List<Account> accountList, String userType) {
        this.accountList = accountList;
        this.userType = userType;  // Set userType value
    }


    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.name.setText(account.getFullName());
        holder.username.setText(account.username);

        if ("super_admin".equals(userType)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= accountList.size()) {
                return; // Invalid position
            }

            Account accountToDelete = accountList.get(currentPosition);

            Toast.makeText(v.getContext(), "Deleting account: " + accountToDelete.getAccountId(), Toast.LENGTH_SHORT).show();

            DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                    .getReference("Administrators")
                    .child(accountToDelete.getAccountId());

            databaseRef.removeValue().addOnSuccessListener(unused -> {
                // Double-check the list size again before removing
                if (currentPosition < accountList.size()) {
                    accountList.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, accountList.size());
                }

                Toast.makeText(v.getContext(), "Account deleted successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(v.getContext(), "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }



    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView name, username;
        View btnDelete;

        public AccountViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtName);
            username = itemView.findViewById(R.id.txtUsername);
            btnDelete = itemView.findViewById(R.id.btnDelete); // ✅ Initialize here
        }
    }
}
