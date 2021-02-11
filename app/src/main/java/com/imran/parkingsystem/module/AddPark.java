package com.imran.parkingsystem.module;

public class AddPark
{
    private String cityname;
    private String pincode;
    private String owneruid;
    private double lat;
    private double lon;
    private boolean bookstatus;


    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getOwneruid() {
        return owneruid;
    }

    public void setOwneruid(String owneruid) {
        this.owneruid = owneruid;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public boolean isBookstatus() {
        return bookstatus;
    }

    public void setBookstatus(boolean bookstatus) {
        this.bookstatus = bookstatus;
    }
}
