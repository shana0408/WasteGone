package com.ntu.cz2006.wastegone.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class WasteLocation {
    @Exclude
    private String id;

    private String requesterUid;
    private String collectorUid;
    private GeoPoint geo_point;
    private String category;
    private String remarks;
    private String imageUri;
    private String status;

    public WasteLocation() {
    }

    public WasteLocation(String requesterUid, String collectorUid, GeoPoint geo_point, String category, String remarks, String imageUri, String status) {
        this.requesterUid = requesterUid;
        this.collectorUid = collectorUid;
        this.geo_point = geo_point;
        this.category = category;
        this.remarks = remarks;
        this.imageUri = imageUri;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequesterUid() {
        return requesterUid;
    }

    public void setRequesterUid(String requesterUid) {
        this.requesterUid = requesterUid;
    }

    public String getCollectorUid() {
        return collectorUid;
    }

    public void setCollectorUid(String collectorUid) {
        this.collectorUid = collectorUid;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
