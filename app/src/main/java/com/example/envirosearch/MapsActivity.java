package com.example.envirosearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.DrawableContainer;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private HttpURLConnection connection;
    private GoogleMap mMap;
    public double lat;
    public double lon;
    public FusedLocationProviderClient client;
    public double radius = 5;
    private boolean boxChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        radius = Double.parseDouble(getIntent().getExtras().getString("radius"));
        boxChecked = getIntent().getExtras().getBoolean("checked");

        client = LocationServices.getFusedLocationProviderClient(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng test = new LatLng(0,0);
        mMap.addMarker(new MarkerOptions().position(test).title("test").icon(BitmapDescriptorFactory.fromResource(R.drawable.adamsmokestack)));

        getLoc();
    }

    public void getLoc() {
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lat = 15; lon = 15;
            return;
        }
        client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if(location != null) { //Finds user location and places a marker on the map, then begins the query
                    Log.d("tag",Double.toString(location.getLongitude()));
                    lat = Math.round(location.getLatitude()*100.0)/100.0;
                    lon = Math.round(location.getLongitude()*100.0)/100.0;
                    LatLng userLoc = new LatLng(lat,lon);
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("Your Location").snippet("http://hgsengineeringinc.com/").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    float zoomLevel = 12.0f;
                    //todo: Add custom zoom level based on the radius
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc,zoomLevel));
                    getJson();
                }

            }
        });

    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }

    private void getJson() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    BufferedReader reader;
                    String line;
                    StringBuffer responseContent = new StringBuffer();

                    try {

                        // Set up and perform get request
                        URL url = new URL("https://ofmpub.epa.gov/echo/echo_rest_services.get_facility_info?output=JSON&p_lat=" + lat + "&p_long="+ lon + "&p_radius=" + radius);
                        connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);

                        int status = connection.getResponseCode();

                        if (status> 299) { // Error code
                            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            showToast("ERROR: Cannot connect to EPA server. Try again.");
                            while((line = reader.readLine()) != null) {
                                responseContent.append(line);
                            }
                            reader.close();
                        }
                        else { // Populate responseContent with info from get request
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            showToast("Searching...");
                            while((line = reader.readLine()) != null) {
                                responseContent.append(line);
                            }
                            reader.close();
                        }

                        parse(responseContent.toString());

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {

                }
            }
        });
        thread.start();
    }

    public void parse(String responseBody) throws JSONException {

        System.out.println(responseBody.length());
        JSONObject facilities = new JSONObject(responseBody); //Data received from the responseBody

        JSONObject Results = new JSONObject(facilities.get("Results").toString()); // JSON object containing JSON arrays and data

        if (Results.getInt("QueryRows") < 4950) {
            JSONArray facList = new JSONArray(Results.get("Facilities").toString()); // A list of JSON objects of facilities
            System.out.println(facList.length());

            // Loops through JSON objects in facList and creates objects for them, then gets their relevant data
            for (int i = 0; i < facList.length(); i++) {
                JSONObject obj = facList.getJSONObject(i);
                final ArrayList<String> facilList = new ArrayList<String>();
                facilList.add(obj.getString("FacName"));
                facilList.add(obj.getString("FacLat"));
                facilList.add(obj.getString("FacLong"));
                facilList.add(obj.getString("RegistryID"));
                facilList.add(obj.getString("FacPenaltyCount"));
                facilList.add(obj.getString("FacQtrsWithNC"));

                //System.out.println(obj.getString("FacQtrsWithNC"));
                //System.out.println(facilList.get(5));


                if (facilList.get(5) != "null") {
                    if (Integer.parseInt(obj.getString("FacQtrsWithNC")) >= 7) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) { // Makes info window click take the user to the facility's EPA page
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getSnippet()));
                                        //startActivity(browserIntent);
                                        Intent facilIntent = new Intent(MapsActivity.this, facilActivity.class);
                                        facilIntent.putExtra("id", Uri.parse(marker.getSnippet()));
                                        startActivity(facilIntent);
                                    }
                                });
                            }
                        });
                    }
                    else if (Integer.parseInt(obj.getString("FacQtrsWithNC")) > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) { // Makes info window click take the user to the facility's EPA page
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getSnippet()));
                                        //startActivity(browserIntent);
                                        Intent facilIntent = new Intent(MapsActivity.this, facilActivity.class);
                                        facilIntent.putExtra("id", Uri.parse(marker.getSnippet()+ ""));
                                        startActivity(facilIntent);
                                    }
                                });
                            }
                        });
                    }
                    else if (Integer.parseInt(obj.getString("FacQtrsWithNC")) == 0 && !boxChecked){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                                mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) { // Makes info window click take the user to the facility's EPA page
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getSnippet()));
                                        startActivity(browserIntent);
                                    }
                                });
                            }
                        });
                    }
                }

            }
            showToast("Finished");
        }
        else {
            showToast("Too many facilities! Please lower radius and try again. ");
        }

    }

    public void showToast(String text) {
        final String textToShow = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.map), textToShow, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }
}

