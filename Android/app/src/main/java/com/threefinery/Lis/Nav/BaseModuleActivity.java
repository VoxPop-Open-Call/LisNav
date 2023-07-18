package com.threefinery.Lis.Nav;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Locale;

public class BaseModuleActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;
    protected Handler mUIHandler;

    private static final String TAG = BaseModuleActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private TextToSpeech textToSpeech = MainActivity.textToSpeech;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private boolean drawAccuracy = true;
    private int accuracyStrokeColor = Color.argb(255, 130, 182, 228);
    private int accuracyFillColor = Color.argb(100, 130, 182, 228);

    private TextToSpeech synthesizer;
    private Marker positionMarker;
    private Circle accuracyCircle;

    public Boolean canStartSceneDescriptor = false;
    public Boolean canStartTimer = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIHandler = new Handler(getMainLooper());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String utteranceId) {
                Log.i("Finished audio", utteranceId);

                if(utteranceId.equals("sceneDescriptor") && !BaseModuleActivity.this.canStartTimer) {
                    BaseModuleActivity.this.canStartTimer = true;
                }
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startBackgroundThread();

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("ModuleActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        stopBackgroundThread();
        super.onDestroy();
    }

    protected void stopBackgroundThread() {
      mBackgroundThread.quitSafely();

      try {
          mBackgroundThread.join();
          mBackgroundThread = null;
          mBackgroundHandler = null;
      } catch (InterruptedException e) {
          Log.e("Object Detection", "Error on stopping background thread", e);
      }
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
    }

    private List<MarkerOptions> loadMapMarkers() {
        List<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
        Location location = null;

        while(true) {
            location = BaseModuleActivity.this.lastKnownLocation;

            if(location != null) {
                break;
            }
        }

        int distanceThreshold = 100;

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

                Location marker = new Location("Marker");
                marker.setLatitude(latLng.latitude);
                marker.setLongitude(latLng.longitude);

                if(location.distanceTo(marker) < distanceThreshold) {
                    String text = "";

                    float degrees = location.bearingTo(marker);
                    float bearing = location.getBearing();

                    int markerQuadrant = 0;
                    int bearingQuadrant = 0;

                    if(degrees < -90 && degrees >= -180) {
                        markerQuadrant = 225;
                    } else if(degrees < 0 && degrees >= -90) {
                        markerQuadrant = 315;
                    }else if(degrees >= 0 && degrees <= 90) {
                        markerQuadrant = 45;
                    } else if(degrees > 90 && degrees <= 180) {
                        markerQuadrant = 135;
                    }

                    if(bearing >= 0 && bearing <= 90) {
                        bearingQuadrant = 45;
                    } else if(bearing > 90 && bearing <= 180) {
                        bearingQuadrant = 135;
                    } else if(bearing > 180 && bearing <= 270) {
                        bearingQuadrant = 225;
                    } else if(bearing > 270 && bearing <= 360) {
                        bearingQuadrant = 315;
                    }

                    int averageQuadrant = markerQuadrant + bearingQuadrant;

                    if(averageQuadrant > 360) {
                        averageQuadrant -= 360;
                    }

                    if(averageQuadrant >= 0 && averageQuadrant <= 90) {
                        text += getResources().getString(R.string.audioFrontRight);
                    } else if(averageQuadrant > 90 && averageQuadrant <= 180) {
                        text += getResources().getString(R.string.audioBackRight);
                    } else if(averageQuadrant > 180 && averageQuadrant <= 270) {
                        text += getResources().getString(R.string.audioBackLeft);
                    } else if(averageQuadrant > 270 && averageQuadrant <= 360) {
                        text += getResources().getString(R.string.audioFrontLeft);
                    }

                    text += type + ", " + name + ". ";

                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, text);
                }
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

                int stringId = R.string.Warning;

                switch (type) {
                    case "Warning":
                        stringId = R.string.Warning;
                        break;
                    case "Hazard":
                        stringId = R.string.Hazard;
                        break;
                    case "Emergency":
                        stringId = R.string.Emergency;
                        break;
                    case "Point of Interest":
                        stringId = R.string.point_of_interest;
                        break;
                    case "Road Closure":
                        stringId = R.string.road_closure;
                        break;
                }

                LatLng latLng = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(name + ". " + getResources().getString(stringId));

                markerOptionsList.add(markerOptions);

                Location marker = new Location("Marker");
                marker.setLatitude(latLng.latitude);
                marker.setLongitude(latLng.longitude);

                if(location.distanceTo(marker) < distanceThreshold) {
                    String text = getResources().getString(R.string.audioNote) + " " + getResources().getString(stringId) + " " + getResources().getString(R.string.audioNoteDescription)  + " " + name;
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, text);
                }
            }

            reader.endArray();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        canStartSceneDescriptor = true;

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
                                getStreetAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            } else {
                                canStartSceneDescriptor = true;
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);

                            canStartSceneDescriptor = true;
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getStreetAddress(double lat, double lng) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                Geocoder gcd = new Geocoder(BaseModuleActivity.this, Locale.getDefault());
                List<Address> addresses = null;

                try {
                    addresses = gcd.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (addresses.size() > 0) {
                    String text = getResources().getString(R.string.audioPosition) + " " + addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getFeatureName() + ". ";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, text);

                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            List<MarkerOptions> markerOptionsList = BaseModuleActivity.this.loadMapMarkers();

                            BaseModuleActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i=0; i < markerOptionsList.size(); i++) {
                                        BaseModuleActivity.this.map.addMarker(markerOptionsList.get(i));
                                    }
                                }
                            });
                        }
                    });

                    thread.start();
                } else {
                    canStartSceneDescriptor = true;
                }
            }
        });

        thread.start();
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

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
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
