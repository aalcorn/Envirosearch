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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;

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
    private boolean CAAChecked = false;
    private boolean CWAChecked = false;
    private boolean RCRAChecked = false;
    private boolean SDWAChecked = false;
    private ImageView legend;
    private Button showhide;
    private ClusterManager mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private String currentID;

    private InterstitialAd mInterstitialAd;

    private SoundPool soundPool;
    private int popSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC,0);

        popSound = soundPool.load(this, R.raw.pop,1);

        MobileAds.initialize(this, "ca-app-pub-1127915110935547~5457208872");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);

        //get parameters from selectActivity
        radius = Double.parseDouble(getIntent().getExtras().getString("radius"));
        boxChecked = getIntent().getExtras().getBoolean("checked");
        CAAChecked = getIntent().getExtras().getBoolean("CAAChecked");
        CWAChecked = getIntent().getExtras().getBoolean("CWAChecked");
        RCRAChecked = getIntent().getExtras().getBoolean("RCRAChecked");
        SDWAChecked = getIntent().getExtras().getBoolean("SDWAChecked");

        legend = findViewById(R.id.imgView);
        showhide = findViewById(R.id.hideButton);

        //toggles visibility of the legend
        showhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (legend.getVisibility() == View.VISIBLE) {
                    legend.setVisibility(View.INVISIBLE);
                    showhide.setText("SHOW");
                }
                else {
                    legend.setVisibility(View.VISIBLE);
                    showhide.setText("HIDE");
                }
            }
        });

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
        mClusterManager = new ClusterManager<ClusterMarker>(MapsActivity.this, mMap);
        mClusterManagerRenderer = new ClusterManagerRenderer(MapsActivity.this, mMap, mClusterManager);
        mClusterManager.setRenderer(mClusterManagerRenderer);


        LatLng test = new LatLng(0,0);
        //mMap.addMarker(new MarkerOptions().position(test).title("test").icon(BitmapDescriptorFactory.fromResource(R.drawable.adamsmokestack)));
        //ClusterMarker marker = new ClusterMarker(test, "test", "testing", R.drawable.thelegend27);
        //mClusterManager.addItem(marker);
        //mClusterManager.cluster();
        getLoc();
    }


    public void getLoc() {
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lat = 15; lon = 15;
            showToast("Location Permissions Denied!");
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
                    //LatLng whiteHouse = new LatLng(38.8977,-77.0365);
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    float zoomLevel = 14.0f; //14.5 good for .5 mile, 11.0 good for 6.5 miles.
                    zoomLevel -= radius*.6;
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
                    showToast("Failed to Connect! Try again.");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mClusterManager.cluster();
                    }
                });

            }
        });
        thread.start();
    }

    public void parse(String responseBody) throws JSONException {

        System.out.println(responseBody.length());
        JSONObject facilities = new JSONObject(responseBody); //Data received from the responseBody

        JSONObject Results = new JSONObject(facilities.get("Results").toString()); // JSON object containing JSON arrays and data

        if (Results.getInt("QueryRows") < 4950) {
            final JSONArray facList = new JSONArray(Results.get("Facilities").toString()); // A list of JSON objects of facilities
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
                final String CAA = obj.getString("CAAComplianceStatus");
                final String CWA = obj.getString("CWAComplianceStatus");
                final String RCRA = obj.getString("RCRAComplianceStatus");
                final String SDWA = obj.getString("SDWAComplianceStatus");





                if (facilList.get(5) != "null") {
                    if (Integer.parseInt(obj.getString("FacQtrsWithNC")) >= 7) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                                if (!CWA.equals("null") && !CWA.equals("Not Applicable") && CWAChecked) {
                                    //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.fromResource(R.drawable.redwater)));
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.redwater);
                                }
                                else if (!CAA.equals("null") && !CAA.equals("Not Applicable") && CAAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.redsmoke);
                                }
                                else if (!RCRA.equals("null") && !RCRA.equals("Not Applicable") && RCRAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.redhazard);
                                }
                                else if (!SDWA.equals("null") && !SDWA.equals("Not Applicable") && SDWAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.reddrinkingwater);
                                }
                                else if(SDWAChecked && RCRAChecked && CAAChecked && CWAChecked){
                                    mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                }
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            }
                        });
                    }
                    else if (Integer.parseInt(obj.getString("FacQtrsWithNC")) > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                                if (!CWA.equals("null") && !CWA.equals("Not Applicable") && CWAChecked) {
                                    //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.fromResource(R.drawable.redwater)));
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.orangewater);
                                }
                                else if (!CAA.equals("null") && !CAA.equals("Not Applicable") && CAAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.orangesmoke);
                                }
                                else if (!RCRA.equals("null") && !RCRA.equals("Not Applicable") && RCRAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.orangehazard);
                                }
                                else if (!SDWA.equals("null") && !SDWA.equals("Not Applicable") && SDWAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.orangedrinkingwater);
                                }
                                else if(SDWAChecked && RCRAChecked && CAAChecked && CWAChecked){
                                    mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                }
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            }
                        });
                    }
                    else if (Integer.parseInt(obj.getString("FacQtrsWithNC")) == 0 && !boxChecked){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng facLoc = new LatLng(Double.parseDouble(facilList.get(1)), Double.parseDouble(facilList.get(2)));
                                if (!CWA.equals("null") && !CWA.equals("Not Applicable") && CWAChecked) {
                                    //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.fromResource(R.drawable.redwater)));
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.greenwater);
                                }
                                else if (!CAA.equals("null") && !CAA.equals("Not Applicable") && CAAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.greensmoke);
                                }
                                else if (!RCRA.equals("null") && !RCRA.equals("Not Applicable") && RCRAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.greenhazard);
                                }
                                else if (!SDWA.equals("null") && !SDWA.equals("Not Applicable") && SDWAChecked) {
                                    makeMarker(facLoc, facilList.get(0), facilList.get(3), R.drawable.greendrinkingwater);
                                }
                                else if(SDWAChecked && RCRAChecked && CAAChecked && CWAChecked){
                                    mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                }
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet("https://echo.epa.gov/detailed-facility-report?fid=" + facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                //mMap.addMarker(new MarkerOptions().position(facLoc).title(facilList.get(0)).snippet(facilList.get(3)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            }
                        });
                    }
                }

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) { // Makes info window click take user to new facility info page
                            if(marker.getTag()!=null) {
                                //String website = "https://echo.epa.gov/detailed-facility-report?fid=" + marker.getTag();
                                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                                //startActivity(browserIntent);
                                Intent facilIntent = new Intent(MapsActivity.this, facilActivity.class);
                                facilIntent.putExtra("id", marker.getTag().toString());
                                startActivity(facilIntent);
                                //System.out.println(marker.getTag());
                            }

                        }
                    });

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if(marker.getSnippet() != null) {
                                marker.setTag(marker.getSnippet());
                                marker.setSnippet(null);
                            }
                            soundPool.play(popSound,1,1,0,0,1);
                            return false;
                        }
                    });
                }
            });
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

    private void makeMarker(LatLng positiion, String title, String snippet, int iconPicture) {
        ClusterMarker newClusterMarker = new ClusterMarker(positiion,title,snippet,iconPicture, "Tag");
        mClusterManager.addItem(newClusterMarker);
        mClusterMarkers.add(newClusterMarker);
    }
}

