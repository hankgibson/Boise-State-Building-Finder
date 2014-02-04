package com.hankgibson.boisestatebuildingfinder;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.UUID;

public class Building
{
    private String name;
    private String description;
    private LatLng position;
    public UUID mId;
    private MarkerOptions marker;

    public Building(){mId = UUID.randomUUID();}
    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    public String getDescription(){return description;}
    public void setDescription(String description){this.description = description;}
    public LatLng getPosition(){return position;}
    public void setPosition(LatLng position){this.position = position;}
    public UUID getId(){return mId;}
    public void setMarker(MarkerOptions mark)
    {
        this.marker = mark;
    }
    public MarkerOptions getMarker()
    {
        return this.marker;
    }
}
