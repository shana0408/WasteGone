package com.ntu.cz2006.wastegone.models;

import com.google.firebase.firestore.GeoPoint;

public class WasteLocation {
    private User requestorUid;
    private User collectorUid;
    private GeoPoint geo_point;
    private String category;
    private String remarks;
    private String imageUri;
    private String status;

    public WasteLocation() {
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getRequestorUid() {
        return requestorUid;
    }

    public void setRequestorUid(User requestorUid) {
        this.requestorUid = requestorUid;
    }

    public User getCollectorUid() {
        return collectorUid;
    }

    public void setCollectorUid(User collectorUid) {
        this.collectorUid = collectorUid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
