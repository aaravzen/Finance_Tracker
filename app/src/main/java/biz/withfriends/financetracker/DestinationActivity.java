package biz.withfriends.financetracker;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class DestinationActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker startLocation;
    private Polyline polyline;

    private boolean notNow = false;
    private boolean running = false;
    private Handler handler = new Handler();
    private final List<LatLng> locationList = new ArrayList<LatLng>();
    private double totalDistance = 0;
    TextView distanceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        EditText location_tf = (EditText) findViewById(R.id.TFAddress);
        location_tf.setImeActionLabel("Go!", KeyEvent.KEYCODE_ENTER);
        //location_tf.setOnEditorActionListener(enterListener);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);



    }

    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.TFAddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;

        if(location != null || !location.equals("") ) {
            Geocoder geocoder = new Geocoder(this);

            try {
                addressList = geocoder.getFromLocationName(location, 1);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            startLocation.remove();
            startLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("Start Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            notNow = true;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            startLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("Start Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }


    public void onReset(View view) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            startLocation.remove();
            startLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("Start Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
        notNow = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO
    }

    /*
    TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                onSearch(exampleView);
            }
            return true;
        }
    }; */

    public void onStartJourney(View view) {
        if (!notNow && !running) {
            running = true;
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                locationList.add(latLng);
                startLocation.remove();
                startLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("Start Location"));
            }


            polyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(locationList)
                    .width(7)
                    .color(Color.BLUE));
        }


        Runnable taskRunner = new Runnable() {
            @Override
            public void run() {
                runTask();
            }
        };

        new Thread(taskRunner).start();
    }


    public void runTask() {
        while (running) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);

                    if (mLastLocation != null) {
                        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        locationList.add(latLng);
                        polyline.remove();
                        polyline = mMap.addPolyline(new PolylineOptions()
                                .addAll(locationList)
                                .width(7)
                                .color(Color.BLUE));

                        for (int i = 1; i < locationList.size(); i++) {
                            double longi2 = locationList.get(i).longitude;
                            double longi1 = locationList.get(i - 1).longitude;
                            double lati2 = locationList.get(i).latitude;
                            double lati1 = locationList.get(i - 1).latitude;


                            double longi = Math.toRadians(Math.abs(longi2 - longi1));
                            double lati = Math.toRadians(Math.abs(lati2 - lati1));

                            longi2 = Math.toRadians(longi2);
                            longi1 = Math.toRadians(longi1);
                            lati2 = Math.toRadians(lati2);
                            lati1 = Math.toRadians(lati1);

                            double a = (Math.sin(lati/2)) * (Math.sin(lati/2)) +
                                    Math.cos(lati1) * Math.cos(lati2) *
                                            (Math.sin(longi/2)) * (Math.sin(longi/2));

                            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

                            double d = 3959 * c;

                            totalDistance += d;
                        }

                        distanceView = (TextView) findViewById(R.id.distanceView);
                        distanceView.setText("Total Distance: " + String.valueOf(totalDistance) + " miles");
                    }

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }



    public void onEndJourney(View view) {
        if (!notNow && running) {
            running = false;
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                locationList.add(latLng);
                polyline.remove();
                polyline = mMap.addPolyline(new PolylineOptions()
                        .addAll(locationList)
                        .width(7)
                        .color(Color.BLUE));
            }

            for (int i = 1; i < locationList.size(); i++) {
                double longi2 = locationList.get(i).longitude;
                double longi1 = locationList.get(i - 1).longitude;
                double lati2 = locationList.get(i).latitude;
                double lati1 = locationList.get(i - 1).latitude;


                double longi = Math.toRadians(Math.abs(longi2 - longi1));
                double lati = Math.toRadians(Math.abs(lati2 - lati1));

                longi2 = Math.toRadians(longi2);
                longi1 = Math.toRadians(longi1);
                lati2 = Math.toRadians(lati2);
                lati1 = Math.toRadians(lati1);

                double a = (Math.sin(lati/2)) * (Math.sin(lati/2)) +
                        Math.cos(lati1) * Math.cos(lati2) *
                                (Math.sin(longi/2)) * (Math.sin(longi/2));

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

                double d = 3959 * c;

                totalDistance += d;
            }

            distanceView = (TextView) findViewById(R.id.distanceView);
            distanceView.setText("Total Distance: " + String.valueOf(totalDistance) + " miles");
        }
    }
}
