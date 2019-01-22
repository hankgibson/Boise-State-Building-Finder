package com.hankgibson.boisestatebuildingfinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoogleMapFragment extends SupportMapFragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
    private ArrayList<Building> mBuildings;
    private LocationManager mLocationManager;
    private GoogleMap mGoogleMap;
    private Display mCurrDisplay;
    private Marker mMyMarker;
    private LatLng mMyLocation;
    private Building currentBuilding;
    private Polyline directions = null;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.app_name);
        MainActivity activity = (MainActivity)getActivity();

        mBuildings = activity.adapter.getList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        WindowManager mWindowManager;
        mWindowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        mCurrDisplay = mWindowManager.getDefaultDisplay();
        mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mGoogleMap = getMap();
        MarkerOptions markerOptions;
        mMyLocation = getMyCurrentLocation();

        if(BuildingController.mSelectedBuildings == null)
        {
            BuildingController.mSelectedBuildings = new ArrayList<Building>();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation,16));
            mGoogleMap.setMyLocationEnabled(true);
            return view;
        }

        currentBuilding = mBuildings.get(MainActivity.currBuilding);
        BuildingController.mSelectedBuildings.add(currentBuilding);
        Building newbldg;


        //add options to marker for custom views

        for(int i = 0; i < BuildingController.mSelectedBuildings.size(); i++)
        {
            markerOptions = new MarkerOptions();


            newbldg = BuildingController.mSelectedBuildings.get(i);

            //get longitude and latitude from the chosen building
            LatLng coordinate = new LatLng(newbldg.getPosition().latitude,
                    newbldg.getPosition().longitude);
            markerOptions.position(coordinate);
            markerOptions.title(newbldg.getName());
            markerOptions.flat(true);
            markerOptions.snippet(getString(R.string.directionsnippet));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            if(BuildingController.mSelectedBuildings.get(i).equals(currentBuilding))
            {
                mMyMarker = mGoogleMap.addMarker(markerOptions);
            }

            else
            {
                mGoogleMap.addMarker(markerOptions);
            }
        }

        //open marker with title open
        mMyMarker.showInfoWindow();

        moveCamera(getMidPoint(currentBuilding, mMyLocation), currentBuilding, mMyLocation);

        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);
        return view;
    }

    public void clearMap()
    {
        mGoogleMap.clear();
        BuildingController.mSelectedBuildings.clear();
    }

    public void clearRoute()
    {
        if(directions != null)
        {
            directions.remove();
        }

    }

    private LatLng getMyCurrentLocation()
    {
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = mLocationManager.getBestProvider(crit, true);
        return new LatLng(mLocationManager.getLastKnownLocation(provider).getLatitude(),
                mLocationManager.getLastKnownLocation(provider).getLongitude());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void moveCamera(LatLng midpoint, Building building, LatLng currPosition)
    {
        Point size = new Point();
        mCurrDisplay.getSize(size);
        int width = size.x;
        int height = size.y;

        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(midpoint);
        b.include(building.getPosition());
        b.include(currPosition);
        LatLngBounds bounds = b.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,128);
        mGoogleMap.moveCamera(cu);
        mGoogleMap.setMyLocationEnabled(true);
    }

    private LatLng getMidPoint(Building currentBuilding, LatLng myLocation)
    {
        double cameraLongitude = (myLocation.longitude + currentBuilding.getPosition().longitude)/2.0;
        double cameraLatitude = (myLocation.latitude + currentBuilding.getPosition().latitude)/2.0;

        return new LatLng(cameraLatitude,cameraLongitude);
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest)
    {
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        //set mode to walking
        String str_mode = "mode=walking";

        // Sensor enabled
        String sensor = "sensor=false";

        Sting metric ="units=metric";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&" + str_mode + metric;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /*
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while(( line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();
            br.close();
        }

        catch(Exception e){Log.d("Exception while downloading url", e.toString());}

        finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        if(directions != null)
        {
            directions.remove();
        }


        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mMyLocation, marker.getPosition());
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if(marker.isInfoWindowShown())
        {
            return true;
        }

        else
            marker.showInfoWindow();
        return true;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>
    {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url)
        {
            // For storing data from web service
            String data = "";

            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }

            catch(Exception e){Log.d("Background Task", e.toString());}
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /* A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >
    {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
        {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try
            {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }

            catch(Exception e){e.printStackTrace();}
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0; i<result.size(); i++)
            {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0; j<path.size(); j++)
                {
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.BLUE);
            }

            directions = mGoogleMap.addPolyline(lineOptions);
        }
    }
}
