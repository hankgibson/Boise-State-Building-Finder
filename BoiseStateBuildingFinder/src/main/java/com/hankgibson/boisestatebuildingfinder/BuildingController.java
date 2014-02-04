package com.hankgibson.boisestatebuildingfinder;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class BuildingController
{
    private static BuildingController ourInstance = new BuildingController();
    private static ArrayList<Building> mBuildings;
    public static ArrayList<Building> getBuildings(){return mBuildings;}
    public static BuildingController getInstance(){return ourInstance;}
    public static ArrayList<Building> mSelectedBuildings;
    public static ArrayList<Marker> mMyMarkers;

    private BuildingController()
    {
        mBuildings = new ArrayList<Building>();
        loadBuildings();
        Collections.sort(mBuildings, new JSONComparator());
    }

    private void loadBuildings()
    {
        Building newBuilding;
        GetREST mGetREST = new GetREST();
        mGetREST.execute(new String[] { "", ""});
        JSONArray buildings = null;
        try
        {
            buildings = mGetREST.get();
        }
        catch (InterruptedException e){e.printStackTrace();}
        catch (ExecutionException e){e.printStackTrace();}

        if (buildings != null)
        {
            try
            {
                for (int i = 0; i < buildings.length(); i++)
                {
                    JSONObject building = buildings.getJSONObject(i);
                    newBuilding = new Building();

                    LatLng newlatlng = new LatLng(building.getJSONObject("location").getDouble("latitude"),
                            building.getJSONObject("location").getDouble("longitude"));

                    //create new building from JSON object
                    newBuilding.setName(building.getString("name"));
                    newBuilding.setDescription(building.getString("description"));
                    newBuilding.setPosition(newlatlng);
                    mBuildings.add(newBuilding);
                }
            }
            catch (JSONException e){e.printStackTrace();}
        }
    }

    public Building getBuilding(UUID id)
    {
        for (Building curr : mBuildings)
        {
            if (curr.getId().equals(id))
                return curr;
        }
        return null;
    }
}
