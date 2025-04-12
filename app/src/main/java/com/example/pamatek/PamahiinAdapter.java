package com.example.pamatek;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PamahiinAdapter extends RecyclerView.Adapter<PamahiinAdapter.ViewHolder> {
    private List<PamahiinItem> itemList;
    private List<PamahiinItem> filteredList;
    private boolean showDeleteButton;

    private boolean showEditButton;
    private DatabaseReference databaseReference;

    public PamahiinAdapter(List<PamahiinItem> itemList, boolean showDeleteButton, boolean showEditButton) {
        this.itemList = itemList;
        this.filteredList = new ArrayList<>(itemList);
        this.showDeleteButton = showDeleteButton;
        this.showEditButton = showEditButton;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Pamahiin");
    }

    // Implementing the required onCreateViewHolder method
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pamahiin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PamahiinItem item = filteredList.get(position);

        // Provide default values if title or description is null
        String title = item.getTitle() != null ? item.getTitle() : "Untitled";
        String description = item.getDescription() != null ? item.getDescription() : "No Description Available";

        holder.titleText.setText(title);
        holder.descriptionText.setText(description);

        // Retrieve the image URL safely
        String imageUrl = item.getImageUrl();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
//            Toast.makeText(holder.itemView.getContext(), "Image URL: " + imageUrl, Toast.LENGTH_SHORT).show();
            // Load image into ImageView using Glide
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder) // Default image while loading
                    .error(R.drawable.error) // Error image if loading fails
                    .into(holder.imageView);
        } else {
            // Set a default image if no URL is available
            holder.imageView.setImageResource(R.drawable.placeholder);
        }

        holder.editButton.setVisibility(showEditButton ? View.VISIBLE : View.GONE);

        holder.editButton.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_dialog, null);

            EditText editTitle = dialogView.findViewById(R.id.editTitle);
            EditText editDescription = dialogView.findViewById(R.id.editDescription);
            ImageView imagePreview = dialogView.findViewById(R.id.imagePreview);
            Button btnSave = dialogView.findViewById(R.id.btnSave);
            ImageButton closeButton = dialogView.findViewById(R.id.btnClose); // Add this ID to your edit_dialog.xml

            editTitle.setText(item.getTitle());
            editDescription.setText(item.getDescription());

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(dialogView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .into(imagePreview);
            } else {
                imagePreview.setImageResource(R.drawable.placeholder);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setView(dialogView);
            AlertDialog editDialog = builder.create();
            editDialog.show();

            // Close dialog
            closeButton.setOnClickListener(view -> editDialog.dismiss());

            // Save updated data
            btnSave.setOnClickListener(view -> {
                String newTitle = editTitle.getText().toString().trim();
                String newDescription = editDescription.getText().toString().trim();
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                if (!newTitle.isEmpty() && !newDescription.isEmpty()) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Confirm Update")
                            .setMessage("Are you sure you want to update?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // User confirmed the update, so update the item
                                item.setTitle(newTitle);
                                item.setDescription(newDescription);
                                item.setDate(currentDate);

                                if (item.getKey() != null) {
                                    // Create a map to update Firebase without including the "key"
                                    Map<String, Object> updatedData = new HashMap<>();
                                    updatedData.put("title", newTitle);
                                    updatedData.put("description", newDescription);
                                    updatedData.put("imageurl", item.getImageUrl());
                                    updatedData.put("current_date", currentDate);

                                    // Save only selected fields to Firebase
                                    databaseReference.child(item.getKey()).setValue(updatedData)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(view.getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                    notifyItemChanged(holder.getAdapterPosition());
                                                } else {
                                                    Toast.makeText(view.getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                                editDialog.dismiss();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Toast.makeText(view.getContext(), "Title and Description cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
        });



        holder.deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String keyToDelete = item.getKey();

                        itemList.remove(adapterPosition);
                        filteredList.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(adapterPosition, filteredList.size());

                        if (keyToDelete != null) {
                            databaseReference.child(keyToDelete).removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(holder.deleteButton.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(holder.deleteButton.getContext(), "Failed to delete from Firebase", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(holder.deleteButton.getContext(), "Error: No key available to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        // Click listener to show details in a dialog
        holder.itemView.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_pamahiin, null);

            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);
            ImageView dialogImageView = dialogView.findViewById(R.id.dialogImage);
            ImageButton closeButton = dialogView.findViewById(R.id.closeButton);

            dialogTitle.setText(title);
            dialogDescription.setText(description);

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(dialogView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .into(dialogImageView);
            } else {
                dialogImageView.setImageResource(R.drawable.placeholder);
            }

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(v.getContext());
            builder.setView(dialogView);
            android.app.AlertDialog dialog = builder.create();
            dialog.show();

            closeButton.setOnClickListener(view -> dialog.dismiss());
        });
    }



    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            for (PamahiinItem item : itemList) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText;
        ImageView imageView;
        ImageButton deleteButton;

        ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            imageView = itemView.findViewById(R.id.imageView); // ImageView in the item layout
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    public void updateData(List<PamahiinItem> newList) {
        this.itemList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
}
