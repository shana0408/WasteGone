package com.ntu.cz2006.wastegone.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ntu.cz2006.wastegone.R;
import com.ntu.cz2006.wastegone.models.WasteLocation;
import com.ntu.cz2006.wastegone.ui.ReservationActivity;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 An adapter to generate recycleview for wastelocation
 @author ILoveNTU
 @version 2.1
 @since 2019-01-15
 */

public class WasteLocationRecyclerAdapter extends RecyclerView.Adapter<WasteLocationRecyclerAdapter.MyViewHolder> {

    private List<WasteLocation> wasteLocationList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView status, category, remarks, geopoints, requestId;
        public ImageView wasteImage;

        public MyViewHolder(View view) {
            super(view);
            requestId = (TextView) view.findViewById(R.id.requestId);
            status = (TextView) view.findViewById(R.id.status);
            category = (TextView) view.findViewById(R.id.category);
            remarks = (TextView) view.findViewById(R.id.remarks);
            geopoints = (TextView) view.findViewById(R.id.geopoints);
            wasteImage = (ImageView) view.findViewById(R.id.waste_image);
        }
    }

    public WasteLocationRecyclerAdapter(List<WasteLocation> wasteLocationList) {
        this.wasteLocationList = wasteLocationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.waste_location_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final WasteLocation wasteLocation = wasteLocationList.get(position);
        holder.requestId.setText(wasteLocation.getId());
        boolean isOpen = wasteLocation.getStatus().equalsIgnoreCase("open");
        boolean isReserve = wasteLocation.getStatus().equalsIgnoreCase("reserved");
        holder.status.setText(isOpen ? "cancel" : isReserve ? "cancel" : wasteLocation.getStatus());
        holder.category.setText("Category： "  + wasteLocation.getCategory());
        holder.remarks.setText("Remarks: " + wasteLocation.getRemarks());
        holder.geopoints.setText(wasteLocation.getGeo_point().toString());
        Picasso.get().load(wasteLocation.getImageUri()).into(holder.wasteImage);

        holder.status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("WasteLocation").document(wasteLocation.getId())
                        .update("status", "open");
                db.collection("WasteLocation").document(wasteLocation.getId())
                        .update("collectorUid", null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wasteLocationList.size();
    }
}
