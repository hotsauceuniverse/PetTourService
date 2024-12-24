package com.seyoung.pettourservice;

import java.io.Serializable;

public class TourTypeData implements Serializable {
    private String title;
    private String tourType;
    private String address;

    public TourTypeData(String title, String tourType, String address) {
        this.title = title;
        this.tourType = tourType;
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTourType() {
        return tourType;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
