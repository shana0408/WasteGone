package com.ntu.cz2006.wastegone.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ntu.cz2006.wastegone.R;
import com.ntu.cz2006.wastegone.models.User;
import com.ntu.cz2006.wastegone.models.WasteLocation;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.ntu.cz2006.wastegone.Constants.*;

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
        public TextView status, category, remarks, address, requestId ,requesterUid, collectorUid, submitDate, collectDate;
        public ImageView wasteImage;

        public MyViewHolder(View view) {
            super(view);
            requestId =  view.findViewById(R.id.requestId);
            status =  view.findViewById(R.id.status);
            category =  view.findViewById(R.id.category);
            remarks =  view.findViewById(R.id.remarks);
            address =  view.findViewById(R.id.geopoints);
            wasteImage = view.findViewById(R.id.waste_image);
            requesterUid =  view.findViewById(R.id.requesterUid);
            collectorUid =  view.findViewById(R.id.collectorUid);
            submitDate =  view.findViewById(R.id.submitDate);
            collectDate =  view.findViewById(R.id.collectDate);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        final WasteLocation wasteLocation = wasteLocationList.get(position);
        holder.requestId.setText(wasteLocation.getId());
        boolean isOpen = wasteLocation.getStatus().equalsIgnoreCase(WASTE_LOCATION_STATUS_OPEN);
        boolean isReserve = wasteLocation.getStatus().equalsIgnoreCase(WASTE_LOCATION_STATUS_RESERVED);
        holder.status.setText(isOpen ? "cancel" : isReserve ? "cancel" : wasteLocation.getStatus());
        holder.category.setText("Category: "  + wasteLocation.getCategory());
        holder.remarks.setText("Remarks: " + wasteLocation.getRemarks());
        holder.address.setText("Address: " + wasteLocation.getAddress());
        holder.submitDate.setText("Submit Date: " +  dateFormat.format(wasteLocation.getSubmitDate()));
        if(wasteLocation.getCollectDate() != null)
        {
            holder.collectDate.setText("Collect Date: " + dateFormat.format(wasteLocation.getCollectDate()));
        }
        db.collection("User").document(wasteLocation.getRequesterUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        holder.requesterUid.setText("Drop by: " + user.getName());
                    }
                });
        if(wasteLocation.getCollectorUid() != null)
        {
            db.collection("User").document(wasteLocation.getCollectorUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);
                            holder.collectorUid.setText("Collect by: " + user.getName());
                        }
                    });
        }
        Picasso.get().load(wasteLocation.getImageUri()).into(holder.wasteImage);

        holder.status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(wasteLocation.getStatus().equalsIgnoreCase(WASTE_LOCATION_STATUS_RESERVED))
                {
                    wasteLocationList.remove(position);
                    notifyItemRemoved(position);
                    db.collection("WasteLocation").document(wasteLocation.getId())
                            .update("status", WASTE_LOCATION_STATUS_OPEN);
                    db.collection("WasteLocation").document(wasteLocation.getId())
                            .update("collectorUid", null);
                }
                else if((wasteLocation.getStatus().equalsIgnoreCase(WASTE_LOCATION_STATUS_OPEN)))
                {
                    wasteLocationList.remove(position);
                    notifyItemRemoved(position);
                    db.collection("WasteLocation").document(wasteLocation.getId())
                            .delete();

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return wasteLocationList.size();
    }

}
