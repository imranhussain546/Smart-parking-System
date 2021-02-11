package com.imran.parkingsystem.module;

public class Module
{
    private String name;
    private int image;

    public Module(String name, int image) {
        this.name = name;
        this.image = image;
    }

    public Module() {
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }



}
