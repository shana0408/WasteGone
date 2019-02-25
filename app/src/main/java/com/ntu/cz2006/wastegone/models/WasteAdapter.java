package com.ntu.cz2006.wastegone.models;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntu.cz2006.wastegone.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WasteAdapter extends RecyclerView.Adapter<WasteAdapter.MyViewHolder> {

    private List<WasteLocation> wasteLocationList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView status, category, remarks, geopoints;
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            status = (TextView) view.findViewById(R.id.status);
            category = (TextView) view.findViewById(R.id.category);
            remarks = (TextView) view.findViewById(R.id.remarks);
            geopoints = (TextView) view.findViewById(R.id.geopoints);
            imageView = (ImageView) view.findViewById(R.id.reservation_image);
        }
    }


    public WasteAdapter(List<WasteLocation> wasteLocationList) {
        this.wasteLocationList = wasteLocationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.waste_location_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WasteLocation wasteLocation = wasteLocationList.get(position);
        holder.status.setText(wasteLocation.getStatus());
        holder.category.setText(wasteLocation.getCategory());
        holder.remarks.setText(wasteLocation.getRemarks());
        holder.geopoints.setText(wasteLocation.getGeo_point().toString());
//        Picasso.get().load(wasteLocation.getImageUri()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return wasteLocationList.size();
    }
}
