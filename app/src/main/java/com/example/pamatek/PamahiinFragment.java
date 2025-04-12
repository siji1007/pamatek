package com.example.pamatek;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PamahiinFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchView searchView;
    private PamahiinAdapter adapter;
    private List<PamahiinItem> itemList;
    private DatabaseReference databaseReference;

    private AlertDialog loadingDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pamahiin, container, false);

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView.setQueryHint("Maghanap ng Pamahiin...");

        // Initialize the list and adapter
        itemList = new ArrayList<>();
        adapter = new PamahiinAdapter(itemList, false, false); // Hide delete button
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Fetch data from Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Pamahiin");
        fetchDataFromFirebase();

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        return view;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    private void fetchDataFromFirebase() {
        if (isNetworkConnected()) {
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

                    // Delay the UI update and dismissing the dialog by 5 seconds
                    new android.os.Handler().postDelayed(() -> {
                        adapter.updateData(itemList);
                        dismissLoadingDialog();
                    }, 5000); // 5000 milliseconds = 5 seconds
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    dismissLoadingDialog();
                }
            });
        } else {
            itemList.clear();
            itemList.add(new PamahiinItem("default", " Huwag iuwi ang unang huli", "Kung iuuwi agad ang unang isda, hindi na susunod ang iba pa.", null));
            itemList.add(new PamahiinItem("default", "Bawal maglayag kapag may buntis sa bahay", " Kapag may buntis sa bahay, dapat umiwas sa dagat ang mangingisda dahil baka maapektuhan ang sanggol.", null));
            itemList.add(new PamahiinItem("default", "Huwag maglayag kapag bilog ang buwan", " Mahirap makahuli ng isda kapag full moon dahil maliwanag ang tubig at nagtatago ang mga isda.", null));

            adapter.updateData(itemList);
            Toast.makeText(getContext(), "No internet connection. Showing default message.", Toast.LENGTH_SHORT).show();
        }
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
