package com.ntu.wastegone.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ntu.wastegone.R;
import com.ntu.wastegone.adapters.WasteLocationRecyclerAdapter;
import com.ntu.wastegone.models.WasteLocation;

import java.util.ArrayList;
import java.util.List;

/**
 ReservationActivity class show a form for user to make reservation and handling its logic
 @author ILoveNTU
 @version 2.1
 @since 2019-01-15
 */

public class ReservationActivity extends AppCompatActivity {
    private List<WasteLocation> wasteLocationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private WasteLocationRecyclerAdapter mAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new WasteLocationRecyclerAdapter(wasteLocationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareWaste();
    }

    private void prepareWaste() {
        final Query wasteLocationCollection = db.collection("WasteLocation").whereEqualTo("collectorUid" , firebaseUser.getUid());

        wasteLocationCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        WasteLocation wasteLocation = document.toObject(WasteLocation.class);
                        wasteLocation.setId(document.getId());
                        wasteLocationList.add(wasteLocation);

             }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
