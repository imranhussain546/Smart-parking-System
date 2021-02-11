package com.imran.parkingsystem.module;

public class Feedback {
    private String message;
    private float rate;
    private String owneruid;
    private String renteruid;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getOwneruid() {
        return owneruid;
    }

    public void setOwneruid(String owneruid) {
        this.owneruid = owneruid;
    }

    public String getRenteruid() {
        return renteruid;
    }

    public void setRenteruid(String renteruid) {
        this.renteruid = renteruid;
    }
}
