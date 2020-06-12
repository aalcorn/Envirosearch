package com.example.envirosearch;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private String tag;
    private int iconPicture;

    public ClusterMarker(LatLng position, String title, String snippet, int iconPicture, String tag) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
        this.tag = tag;
    }

    public ClusterMarker() {
        iconPicture = R.drawable.adamsimage;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public String getTag() {
        return tag;
    }
}
