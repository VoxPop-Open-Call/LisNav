// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.threefinery.Lis.Nav;

import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = MapViewActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation = null;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private boolean drawAccuracy = true;
    private int accuracyStrokeColor = Color.argb(255, 130, 182, 228);
    private int accuracyFillColor = Color.argb(100, 130, 182, 228);

    private Marker positionMarker;
    private Circle accuracyCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_annotation);

        @Nullable ApplicationInfo applicationInfo = null;

        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        @Nullable String apiKey = null;

        if(applicationInfo != null) {
            apiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY");
        }

        Places.initialize(getApplicationContext(), apiKey);
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{
                getResources().getString(R.string.Warning),
                getResources().getString(R.string.Hazard),
                getResources().getString(R.string.Emergency),
                getResources().getString(R.string.point_of_interest),
                getResources().getString(R.string.road_closure)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown = findViewById(R.id.spinner2);
        items = new String[]{
                "6 " + getResources().getString(R.string.Hours),
                "12 " + getResources().getString(R.string.Hours),
                "24 " + getResources().getString(R.string.Hours),
                "3 " + getResources().getString(R.string.Days),
                "7 " + getResources().getString(R.string.Days),
                "15 " + getResources().getString(R.string.Days),
                "30 " + getResources().getString(R.string.Days)
        };

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        final Button buttonAnnotation = findViewById(R.id.sendButton);
        buttonAnnotation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        if(lastKnownLocation != null) {
                            EditText mEdit = (EditText)findViewById(R.id.editTextText);
                            Spinner typeSpinner = (Spinner) findViewById(R.id.spinner1);

                            int type = typeSpinner.getSelectedItemPosition();
                            String typeLabel = "Warning";

                            switch (type) {
                                case 0:
                                    typeLabel = "Warning";
                                    break;
                                case 1:
                                    typeLabel = "Hazard";
                                    break;
                                case 2:
                                    typeLabel = "Emergency";
                                    break;
                                case 3:
                                    typeLabel = "Point of Interest";
                                    break;
                                case 4:
                                    typeLabel = "Road Closure";
                                    break;
                            }

                            Spinner timeSpinner = (Spinner) findViewById(R.id.spinner2);

                            int time = timeSpinner.getSelectedItemPosition();
                            int hours = 6;

                            switch (time) {
                                case 0:
                                    hours = 6;
                                    break;
                                case 1:
                                    hours = 12;
                                    break;
                                case 2:
                                    hours = 24;
                                    break;
                                case 3:
                                    hours = 72;
                                    break;
                                case 4:
                                    hours = 168;
                                    break;
                                case 5:
                                    hours = 360;
                                    break;
                                case 6:
                                    hours = 720;
                                    break;
                            }

                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("https")
                                    .authority("api.3finery.com")
                                    .appendPath("LisNav")
                                    .appendPath("setEvent.php")
                                    .appendQueryParameter("type", typeLabel)
                                    .appendQueryParameter("hours", String.valueOf(hours))
                                    .appendQueryParameter("name", mEdit.getText().toString())
                                    .appendQueryParameter("latitude", String.valueOf(lastKnownLocation.getLatitude()))
                                    .appendQueryParameter("longitude", String.valueOf(lastKnownLocation.getLongitude()))
                                    .appendQueryParameter("key", "0CteP3mPexoVQDM6CCzf");

                            String requestURL = builder.build().toString();
                            URL url = null;

                            try {
                                try {
                                    url = new URL(requestURL);
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }

                                HttpURLConnection urlConnection = null;

                                try {
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                try {
                                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                    reader.beginObject();

                                    String name = reader.nextName();
                                    Boolean success = false;

                                    if (name.equals("success")) {
                                        success = reader.nextBoolean();
                                    }

                                    reader.endObject();
                                    reader.close();

                                    if(success) {
                                        MapViewActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MapViewActivity.this);
                                                builder1.setMessage(getResources().getString(R.string.SuccessMessage));
                                                builder1.setCancelable(false);

                                                builder1.setPositiveButton(
                                                        getResources().getString(R.string.legalAccept),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                finish();
                                                            }
                                                        });

                                                AlertDialog alert11 = builder1.create();
                                                alert11.show();
                                            }
                                        });
                                    } else {
                                        MapViewActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MapViewActivity.this);
                                                builder1.setMessage(getResources().getString(R.string.ErrorServer));
                                                builder1.setCancelable(false);

                                                builder1.setPositiveButton(
                                                        getResources().getString(R.string.legalAccept),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                finish();
                                                            }
                                                        });

                                                AlertDialog alert11 = builder1.create();
                                                alert11.show();
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    urlConnection.disconnect();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                });

                thread.start();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                List<MarkerOptions> markerOptionsList = MapViewActivity.this.loadMapMarkers();

                MapViewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i < markerOptionsList.size(); i++) {
                            MapViewActivity.this.map.addMarker(markerOptionsList.get(i));
                        }
                    }
                });
            }
        });

        thread.start();
    }

    private List<MarkerOptions> loadMapMarkers() {
        List<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(getAssets().open("dataset.json"), "UTF-8"));

            reader.beginArray();

            while (reader.hasNext()) {
                reader.beginObject();

                reader.nextName();
                Double latitude = reader.nextDouble();

                reader.nextName();
                Double longitude = reader.nextDouble();

                reader.nextName();
                int category = reader.nextInt();

                reader.nextName();
                String name = reader.nextString();

                reader.nextName();
                String type = reader.nextString();

                reader.endObject();

                LatLng latLng = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(name + ". " + type);

                markerOptionsList.add(markerOptions);
            }

            reader.endArray();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        URL url = null;

        try {
            url = new URL("https://api.3finery.com/LisNav/getEvents.php");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();

            while (reader.hasNext()) {
                reader.beginObject();

                reader.nextName();
                int id = reader.nextInt();

                reader.nextName();
                String name = reader.nextString();

                reader.nextName();
                String type = reader.nextString();

                reader.nextName();
                Double latitude = reader.nextDouble();

                reader.nextName();
                Double longitude = reader.nextDouble();

                reader.endObject();

                LatLng latLng = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(name + ". " + type);

                markerOptionsList.add(markerOptions);
            }

            reader.endArray();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return markerOptionsList;
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();

        if (positionMarker != null) {
            positionMarker.remove();
        }
        final MarkerOptions positionMarkerOptions = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f);
        positionMarker = map.addMarker(positionMarkerOptions);

        if (accuracyCircle != null) {
            accuracyCircle.remove();
        }
        if (drawAccuracy) {
            final CircleOptions accuracyCircleOptions = new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(accuracy)
                    .fillColor(accuracyFillColor)
                    .strokeColor(accuracyStrokeColor)
                    .strokeWidth(2.0f);
            accuracyCircle = map.addCircle(accuracyCircleOptions);
        }

        Log.v("latitude", String.valueOf(latitude));
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
