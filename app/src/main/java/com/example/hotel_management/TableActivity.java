package com.example.hotel_management;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.hotel_management.datatypes.Table;
import com.example.hotel_management.recyledview.TableAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TableActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TableAdapter tableAdapter;
    private ArrayList<Table> tables;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        recyclerView = findViewById(R.id.tableGrid);
        tables = new ArrayList<>();
        tableAdapter = new TableAdapter(tables);
        recyclerView.setAdapter(tableAdapter);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
        tableAdapter.setOnStartSessionListener(table -> {
            Intent intent = new Intent(TableActivity.this, MenuActivity.class);
            intent.putExtra("tableID", table.tableID);
            startActivity(intent);
        });
        tableAdapter.setOnEndSessionListener(table -> {
                    table.status = "Available";
                    tableAdapter.notifyDataSetChanged();
                    db.collection("tables").document(table.tableID.toString()).update("status", "Available").addOnSuccessListener(documentReference -> {
                        Log.d("FirestoreData", "status successfully updated!");
                    }).addOnFailureListener(e -> {
                        Log.d("FirestoreData", "Error updating status", e);
                    });
                    //for now we are not updating the session status
//                    db.collection("sessions").document(table.lastSessionID).update("checkedOut", true).addOnSuccessListener(documentReference -> {
//                        Log.d("FirestoreData", "checkedOut successfully updated!");
//                    }).addOnFailureListener(e -> {
//                        Log.d("FirestoreData", "Error updating checkedOut", e);
//                    });
                }
        );

        tableAdapter.setOnTableDetailsListener(table -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_table_info);
            bottomSheetDialog.getBehavior().setMaxWidth(2000);
            bottomSheetDialog.show();
        });
        db = FirebaseFirestore.getInstance();
        db.collection("tables").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isEmpty()){
                Log.d("FirestoreData", "No tables found");
                return;
            }
            tables.clear();
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Integer tableID = documentSnapshot.getLong("tableID").intValue();
                String tableStatus = documentSnapshot.getString("status");
                String lastSessionID = documentSnapshot.getString("lastSessionID");
                Table table = new Table(tableID, tableStatus, lastSessionID);
                tables.add(table);
            }
            tableAdapter.notifyDataSetChanged();
        });
        db.collection("tables").addSnapshotListener((value, error) -> {
               if (error != null) {
                 Log.d("FirestoreData", "Error getting documents: ", error);
                 return;
                }
            tables.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot documentSnapshot : value) {
                Integer tableID = documentSnapshot.getLong("tableID").intValue();
                String tableStatus = documentSnapshot.getString("status");
                String lastSessionID = documentSnapshot.getString("lastSessionID");
                Table table = new Table(tableID, tableStatus, lastSessionID);
                tables.add(table);
            }
            tableAdapter.notifyDataSetChanged();
        });

    }
}