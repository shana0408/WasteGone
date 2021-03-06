package com.ntu.wastegone.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

/**
 WasteLocation class store waste material's information
 @author ILoveNTU
 @version 2.1
 @since 2019-01-15
 */

public class WasteLocation {

    /**
     * FireStore Document reference id
     */
    @Exclude
    private String id;

    /**
     * requester id
     */
    private String requesterUid;
    /**
     * collector id
     */
    private String collectorUid;
    /**
     * waste material's geo point
     */
    private GeoPoint geo_point;
    /**
     * waste material's category
     */
    private String category;
    /**
     * remark from requester
     */
    private String remarks;
    /**
     * image
     */
    private String imageUri;
    /**
     * request status
     */
    private String status;

    private String address;

    private Date submitDate;

    private Date collectDate;

    public WasteLocation() {
    }

    /**
     * Create new wastelocation with requester id, collector id, geo point, category, remarks, image and status
     * @param requesterUid requester id.
     * @param collectorUid collector id.
     * @param geo_point wastelocation's geo point.
     * @param category wastelocation's category.
     * @param remarks requester's remark.
     * @param imageUri requester upload image.
     * @param status request's status.
     */
    public WasteLocation(String requesterUid, String collectorUid, GeoPoint geo_point, String category, String remarks, String imageUri, String status, String address, Date submitDate,  Date collectDate) {
        this.requesterUid = requesterUid;
        this.collectorUid = collectorUid;
        this.geo_point = geo_point;
        this.category = category;
        this.remarks = remarks;
        this.imageUri = imageUri;
        this.status = status;
        this.address = address;
        this.submitDate = submitDate;
        this.collectDate = collectDate;
    }

    /**
     * Gets unique ID.
     * @return unique ID.
     */
    @Exclude
    public String getId() {
        return id;
    }

    /**
     * Changes unique id
     * @param id set unique id.
     */
    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the requester ID.
     * @return requester ID.
     */
    public String getRequesterUid() {
        return requesterUid;
    }
    /**
     * Changes requester id
     * @param requesterUid set requester id.
     */
    public void setRequesterUid(String requesterUid) {
        this.requesterUid = requesterUid;
    }

    /**
     * Gets the collector ID.
     * @return collector ID.
     */
    public String getCollectorUid() {
        return collectorUid;
    }
    /**
     * Changes collecter id
     * @param collectorUid set collecter id.
     */
    public void setCollectorUid(String collectorUid) {
        this.collectorUid = collectorUid;
    }

    /**
     * Gets the geo point.
     * @return geo point.
     */
    public GeoPoint getGeo_point() {
        return geo_point;
    }

    /**
     * Changes geopoint
     * @param geo_point set geopoint.
     */
    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    /**
     * Gets the category.
     * @return category.
     */
    public String getCategory() {
        return category;
    }
    /**
     * Changes category
     * @param category set category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the remark.
     * @return remark.
     */
    public String getRemarks() {
        return remarks;
    }
    /**
     * Changes remarks
     * @param remarks set remarks.
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Gets the image.
     * @return image.
     */
    public String getImageUri() {
        return imageUri;
    }
    /**
     * Changes image
     * @param imageUri set image.
     */
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * Gets the status.
     * @return status.
     */
    public String getStatus() {
        return status;
    }
    /**
     * Changes status
     * @param status set status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {return  address;}

    public  void setAddress(String address) { this.address = address;}

    public Date getSubmitDate() {return  submitDate;}

    public  void setSubmitDate(Date submitDate) { this.submitDate = submitDate;}

    public Date getCollectDate() {
        return collectDate;
    }

    public void setCollectDate(Date collectDate) {
        this.collectDate = collectDate;
    }
}
