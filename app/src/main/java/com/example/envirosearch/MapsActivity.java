package com.example.envirosearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.concurrent.CountDownLatch;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //public ArrayList<ArrayList<String>> facilList;
    private HttpURLConnection connection;
    private GoogleMap mMap;
    public double lat;
    public double lon;
    public FusedLocationProviderClient client;
    public boolean isFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        getLoc();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //LatLng bham = new LatLng(33.5,-83);
        //mMap.addMarker(new MarkerOptions().position(bham).title("Birmingham AL"));


        //LatLng userLoc = new LatLng(lat,lon);
        //mMap.addMarker(new MarkerOptions().position(userLoc).title("This is u!"));

        //LatLng businessLoc = new LatLng(Double.parseDouble(facilList.get(0).get(1)),Double.parseDouble(facilList.get(0).get(2)));
        //mMap.addMarker(new MarkerOptions().position(businessLoc).title(facilList.get(0).get(0)));

        System.out.println("This code runs now.");
    }

    public void getLoc() {
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lat = 15; lon = 15;
            System.out.println("Permission denied!");
            return;
        }
        System.out.println("Permission granted!");
        client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if(location != null) {
                    System.out.println(location.getLatitude());
                    Log.d("tag",Double.toString(location.getLongitude()));
                    lat = Math.round(location.getLatitude()*100.0)/100.0;
                    lon = Math.round(location.getLongitude()*100.0)/100.0;
                    LatLng userLoc = new LatLng(lat,lon);
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("This is u!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoc));
                    //LatLng facLoc = new LatLng(37.39,-122.07);
                    //mMap.addMarker(new MarkerOptions().position(facLoc).title("Some Fac"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(facLoc));
                    getJson();
                }

            }
        });

    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }

    private void getJson() {
        //LatLng facLoc = new LatLng(37.39,-122.07);
        //mMap.addMarker(new MarkerOptions().position(facLoc).title("Some Fac"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(facLoc));
        final GoogleMap jsonMap = mMap;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                //LatLng facLoc = new LatLng(37.39,-122.07);
                //mMap.addMarker(new MarkerOptions().position(facLoc).title("Some Fac")); //DOES NOT WORK
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(facLoc));
                try {
                    BufferedReader reader;
                    String line;
                    StringBuffer responseContent = new StringBuffer();
                    //LatLng facLoc = new LatLng(37.39,-122.07);
                    //mMap.addMarker(new MarkerOptions().position(facLoc).title("Some Fac")); WORKS W/O THREAD
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(facLoc));
                    try {
                        System.out.println(lat);
                        System.out.println(lon);
                        URL url = new URL("https://ofmpub.epa.gov/echo/echo_rest_services.get_facility_info?output=JSON&p_lat=" + lat + "&p_long="+ lon + "&p_radius=4");
                        //URL url = new URL("https://jsonplaceholder.typicode.com/albums");
                        connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);

                        int status = connection.getResponseCode();
                        System.out.println(status);

                        if (status> 299) {
                            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            while((line = reader.readLine()) != null) {
                                responseContent.append(line);
                            }
                            reader.close();
                        }
                        else {
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            while((line = reader.readLine()) != null) {
                                responseContent.append(line);
                                //System.out.println(line);
                            }
                            reader.close();
                        }

                        //System.out.println(responseContent.toString());
                        //System.out.println(responseContent.length());
                        //System.out.println(parse(responseContent.toString()));
                        //facilList = parse(responseContent.toString());
                        //System.out.println(facilList.get(0).get(0));
                        parse(responseContent.toString());
                        //System.out.println("Before the map interaction");

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

        /*final ArrayList<ArrayList<String>> facilList;
        facilList = new ArrayList<ArrayList<String>>();
        facilList.add(new ArrayList<String>());
        */
        //facilList.get(0).add("First Element");
        //facilList.get(0).add("Second Element");
        //facilList.get(0).add("Third Element");

        // Add an element to the first dimension
        //aObject.add(new ArrayList<String>());

        // Place a string in position [0,0]
        //aObject.get(0).add(new String("Quarks"));

        // Display the string in position [0,0]
        //println(aObject.get(0).get(0).toString());

        System.out.println(responseBody.length());
        JSONObject facilities = new JSONObject(responseBody);
        //System.out.println(facilities.length());
        //System.out.println(facilities.toString());
        //System.out.println(facilities.get("Results"));
        JSONObject Results = new JSONObject(facilities.get("Results").toString());
        //System.out.println(Results.toString());
        //System.out.println(Results.get("QueryParameters"));
        //System.out.println(Results.get("Facilities"));
        JSONArray facList = new JSONArray(Results.get("Facilities").toString());
        //System.out.println(facList.toString());
        System.out.println(facList.length());
        //JSONObject obj = facList.getJSONObject(0);
        //System.out.println(obj.toString());
        /*System.out.println(obj.getString("FacName"));
        facilList.get(0).add(obj.getString("FacName"));
        facilList.get(0).add(obj.getString("FacLat"));
        facilList.get(0).add(obj.getString("FacLong"));*/

        for (int i = 0; i < facList.length(); i++) {
            JSONObject obj = facList.getJSONObject(i);
            final ArrayList<String> facilList = new ArrayList<String>();
            facilList.add(obj.getString("FacName"));
            facilList.add(obj.getString("FacLat"));
            facilList.add(obj.getString("FacLong"));
            facilList.add(obj.getString("RegistryID"));


            if (obj.getString("FacPenaltyCount") != "null") {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                        mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(facLoc));
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getSnippet()));
                                startActivity(browserIntent);
                            }
                        });
                    }
                });
            }
        }


        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(0).get(1)),Double.parseDouble(facilList.get(0).get(2)));
                mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0).get(0)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(facLoc));
            }
        });*/

        //System.out.println(Results.getClass()); //JSONObject
        //System.out.println(Results.get("Facilities").getClass());
        //JSONObject facList = new JSONObject(Results.get("Facilities"));
        //System.out.println(facList.toString());

        //System.out.println(facilities.getJSONObject(0).toString());
        /*for (int i = 1; i < facilities.length(); i++) {
            JSONObject facility = facilities.getJSONObject(i);
            String name = facility.getString("Results");
            System.out.println(name);
            System.out.println("test2");
        }*/
    }
}
