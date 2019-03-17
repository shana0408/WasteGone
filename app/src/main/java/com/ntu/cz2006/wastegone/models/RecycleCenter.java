package com.ntu.cz2006.wastegone.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class RecycleCenter {
    @Exclude
    private String id;
    private String address;
    private String name;
    private GeoPoint geoPoint;

    public RecycleCenter() {

    }

    public RecycleCenter(String address, String name, GeoPoint geoPoint) {
        this.address = address;
        this.name = name;
        this.geoPoint = geoPoint;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
