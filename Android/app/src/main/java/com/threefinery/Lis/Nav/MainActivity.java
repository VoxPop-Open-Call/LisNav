package com.threefinery.Lis.Nav;

import com.threefinery.Lis.Nav.BuildConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Debug;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;

import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private Module mModule = null;
    public static TextToSpeech textToSpeech;
    private SharedPreferences mPrefs;
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
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

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            setupMainLayout();
        }
    }

    private void setupMainLayout() {
        setContentView(R.layout.activity_main);

        final Button buttonLive = findViewById(R.id.liveButton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

        final Button buttonAnnotation = findViewById(R.id.annotationButton);
        buttonAnnotation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, MapViewActivity.class);
                startActivity(intent);
            }
        });

        final Button buttonAbout = findViewById(R.id.aboutButton);
        buttonAbout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        final Button buttonLocation = findViewById(R.id.locationButton);
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        if(lastKnownLocation != null) {
                            MainActivity.this.getStreetAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            MainActivity.this.loadMapMarkers();
                        } else {
                            refreshDeviceLocation();
                        }
                    }
                });

                thread.start();
            }
        });

        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "yolov5s.torchscript.ptl"));

            String key = getResources().getString(R.string.audioLanguage);
            String fileName = "classes_" + key + ".txt";

            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));

            String line;
            List<String> classes = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                classes.add(line);
            }

            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    String key = getResources().getString(R.string.audioLanguage);
                    Locale locale = new Locale.Builder().setLanguageTag(key).build();

                    textToSpeech.setLanguage(locale);
                }
            }
        });

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean hasAcceptedTerms = mPrefs.getBoolean("hasAcceptedTerms", false);

        int numAppOpened = mPrefs.getInt("numberTimesOpened", 0);
        numAppOpened++;

        int lastVersionPromptedForReview = mPrefs.getInt("openedVersion", 0);
        int currentVersionCode = BuildConfig.VERSION_CODE;

        if (!hasAcceptedTerms) {
            Intent intent = new Intent(this, LegalActivity.class);
            startActivity(intent);
        }

        if(numAppOpened > 2 && currentVersionCode != lastVersionPromptedForReview) {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();

            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putInt("openedVersion", BuildConfig.VERSION_CODE);
                    editor.commit();

                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo);
                }
            });
        }

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("numberTimesOpened", numAppOpened);
        editor.commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapUserLocation);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            location = MainActivity.this.lastKnownLocation;

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

    private void refreshDeviceLocation() {
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

                                MainActivity.this.getStreetAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                MainActivity.this.loadMapMarkers();
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

    private void getStreetAddress(double lat, double lng) {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (addresses.size() > 0) {
            String text = getResources().getString(R.string.audioPosition) + " " + addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getFeatureName() + ". ";

            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, text);
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

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
