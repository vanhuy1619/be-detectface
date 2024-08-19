package com.example.myapplication.model;

import java.io.Serializable;

public class ImageDetect implements Serializable {
    private String colormap;
    private String visimage;

    public ImageDetect(String colormap, String visimage) {
        this.colormap = colormap;
        this.visimage = visimage;
    }


    public String getColormap() {
        return colormap;
    }

    public void setColormap(String colormap) {
        this.colormap = colormap;
    }

    public String getVisimage() {
        return visimage;
    }

    public void setVisimage(String visimage) {
        this.visimage = visimage;
    }
}
