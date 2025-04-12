package com.example.pamatek;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import java.io.File;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;  // Add Glide for image loading

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

public class Pamahiin_add extends Fragment {



    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText titleInput, descriptionInput, imageUrlInput;  // Add reference to the new EditText for Image URL
    private ImageView imageView;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private PamahiinAdapter adapter;
    private List<PamahiinItem> itemList;
    private Cloudinary cloudinary;  // Declare Cloudinary object

    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pamahiin_add, container, false);

        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        imageUrlInput = view.findViewById(R.id.imageUrlInput);  // Initialize the Image URL input field
        imageView = view.findViewById(R.id.imageView);
        Button addButton = view.findViewById(R.id.addButton);
        recyclerView = view.findViewById(R.id.recyclerView);

        databaseReference = FirebaseDatabase.getInstance().getReference("Pamahiin");
        itemList = new ArrayList<>();
        adapter = new PamahiinAdapter(itemList, true, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dz5nz06an",
                "api_key", "476913494983885",
                "api_secret", "P_TdFdfr_bOcDjzDsI-yJbW-Im0"));

        fetchDataFromFirebase();

        addButton.setOnClickListener(v -> addNewItem());

        imageView.setOnClickListener(v -> openImageChooser());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToCloudinary(imageUri);  // Upload image to Cloudinary
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);

            // Show loading dialog before starting upload
            showLoadingDialog();

            new Thread(() -> {
                try {
                    Map<String, Object> result = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());

                    getActivity().runOnUiThread(() -> {
                        dismissLoadingDialog(); // Dismiss after successful upload

                        String imageUrl = result.get("url").toString();  // Get the uploaded image URL
                        if (imageUrl.startsWith("http://")) {
                            imageUrl = imageUrl.replace("http://", "https://");
                        }

                        imageUrlInput.setText(imageUrl);
                        loadImage(imageUrl);
                        Toast.makeText(getActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    });

                } catch (IOException e) {
                    getActivity().runOnUiThread(() -> {
                        dismissLoadingDialog(); // Dismiss even if there's an error
                        Log.e("Cloudinary", "Upload failed", e);
                        Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

        } catch (FileNotFoundException e) {
            Log.e("Pamahiin_add", "File not found", e);
            Toast.makeText(getActivity(), "Image not found", Toast.LENGTH_SHORT).show();
        }
    }



    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && ((Cursor) cursor).moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }

    private void loadImage(String url) {
        Glide.with(getContext())
                .load(url)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(imageView);
    }

    private void addNewItem() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (title.isEmpty() || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(getActivity(), "Pakiusap, punan po ang lahat ng patlang.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog(); // ⏳ Show loading

        String key = databaseReference.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("current_date", currentDate);
        data.put("imageurl", imageUrl);

        if (key != null) {
            databaseReference.child(key).setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        dismissLoadingDialog(); // ✅
                        Toast.makeText(getActivity(), "Matagumpay na naidagdag.", Toast.LENGTH_SHORT).show();
                        resetFields();
                    })
                    .addOnFailureListener(e -> {
                        dismissLoadingDialog(); // ❌
                        Toast.makeText(getActivity(), "Nabigong maidagdag ang datos.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            dismissLoadingDialog();
            Toast.makeText(getActivity(), "Nabigong makabuo ng susi.", Toast.LENGTH_SHORT).show();
        }
    }


    private void resetFields() {
        titleInput.setText("");
        descriptionInput.setText("");
        imageUrlInput.setText("");
        imageView.setImageResource(android.R.color.transparent);
    }

    private void fetchDataFromFirebase() {
        showLoadingDialog(); 

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String imageUrl = snapshot.child("imageurl").getValue(String.class);
                    String key = snapshot.getKey();


                    if (title != null && description != null && imageUrl != null && key != null) {
                        itemList.add(new PamahiinItem(key, title, description, imageUrl));
                    }
                }

                new android.os.Handler().postDelayed(() -> {
                    adapter.updateData(itemList);
                    dismissLoadingDialog();
                }, 5000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissLoadingDialog();
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





