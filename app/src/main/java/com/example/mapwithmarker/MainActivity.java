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

package com.example.mapwithmarker;

import static com.example.mapwithmarker.Database.DB_TABLE_NAME;
import static com.google.android.gms.maps.UiSettings.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {


    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    int colPrimary, colAccent, currentPage=0, previousPage;
    final int MAX_PAGES = 3;
    LinearLayout[] main_pages;
    Animation translateLeftIn, translateRightIn, translateLeftOut, translateRightOut;

    private PlacesClient placesClient;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    final float ZOOM_WEIGHT = 6.0f;
    float zoom = ZOOM_WEIGHT + 5.0f;
    boolean zoomInControl = false;

    Marker selectedMarker;


    //Server server;
    //final String PHP_SERVER_URL = "http://121.124.124.95/PHP_connection.php";

    Button btn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            ??????????????? ?????? ?????? (build.gradle (:app)?????? ?????? api ????????? 21???????????? ????????????)
            ?????? ????????? ?????????????????????.
            https://calvinjmkim.tistory.com/9
            https://figureking.tistory.com/351
         */
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        int colNavi = ContextCompat.getColor(MainActivity.this, R.color.colorAccent);
        getWindow().setNavigationBarColor(colNavi);

        setContentView(R.layout.layout_main);

        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "????????? Text";

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing = Intent.createChooser(Sharing_intent, "????????????");
                startActivity(Sharing);
            }
        });

         */

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        /*
            ??????&?????? ?????? ??????. ?????? ????????? ?????????????????????.
            https://developers.google.com/maps/documentation/android-sdk/controls?hl=ko#zoom_controls

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

         */

        /*
            ????????? ???????????????. mMarker ???????????? ?????? ????????? ????????? ??????????????? ????????? ????????? ??????????????????.
            ?????? ????????? ?????? ???????????? [?????? Activity, ??????(LatLng), ??????]??? ???????????????.
         */
        LatLng loc = new LatLng(36.99502, 127.13327);
        //mMarker marker = new mMarker(MainActivity.this, loc, googleMap);
        //marker.focus(18);

        /*
            ????????? ?????? ?????? ??? ?????? ???????????? ???????????? ???????????????.
         */
        map.setInfoWindowAdapter(new mInfoWindowAdapter(MainActivity.this));

        /*
            ????????? ??????/?????? ??? ????????? ???????????????.
         */
        map.setMinZoomPreference(6);
        map.setMaxZoomPreference(20);

        onActivityReady();

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /*
        ????????? ?????? ??? ?????? onCreate?????? ??????????????? ?????? ????????? ???????????? ?????? ???????????????.
        ????????? ?????? ???????????? ???????????? onMapReady??? ?????? ?????????, mapFragment.getMapAsync??? ?????? ????????????
        ????????? ???????????? ???????????????.
     */
    @SuppressLint("PotentialBehaviorOverride")
    private void onActivityReady() {

        /*
            ????????? FrameLayout??? ????????? ?????????+???????????? ???????????? ??????????????? ??????????????????,
            Button ?????? LinearLayout?????? clickable ????????? true??? ?????? ???, ?????? ???????????? ?????????????????????.
         */

        colPrimary = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);
        colAccent = ContextCompat.getColor(MainActivity.this, R.color.colorAccent);




        /*
            main_pages: FrameLayout??? ????????? ??? ??????????????? ???????????? ???????????????.
            ?????? ??????, main_pages[1]??? ????????? 1, ??? ????????? ??????????????????.
            ???????????? ????????? ??? movePage() ???????????? ???????????? ?????????????????? ????????? ????????? ????????? ???????????? ???????????????.
            ?????? ?????? ?????? ???????????? ????????? ????????? ???????????? ????????? ???????????? ??? ?????? ???????????? ????????? ??? ????????????.
         */
        main_pages = new LinearLayout[MAX_PAGES];
        main_pages[0] = (LinearLayout) findViewById(R.id.main_pages_0);
        main_pages[1] = (LinearLayout) findViewById(R.id.main_pages_1);
        main_pages[1].addView(View.inflate(this, R.layout.page_1, null));
        main_pages[2] = (LinearLayout) findViewById(R.id.main_pages_2);
        main_pages[2].addView(View.inflate(this, R.layout.page_2, null));
        /* ????????? ?????? ?????? (n ?????? ??????)
        main_pages[n] = (LinearLayout) findViewById(R.id.main_pages_n);
        main_pages[n].addView(View.inflate(this, R.layout.page_n, null));
         */

        findViewById(R.id.page_1_add_marker).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_1_reload_markers).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_1_reset_all_markers).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_1_back_to_main).setOnClickListener(mOnclickListener);

        findViewById(R.id.page_2_save_changes).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_2_share_marker).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_2_delete_marker).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_2_back_to_main).setOnClickListener(mOnclickListener);

        /*
            ????????? ?????? ??????????????? ?????? & ????????? ???????????????. ?????? ????????? ?????????????????????.
            IN??? ????????? ???????????? ?????????(newPage), OUT??? ????????? ????????? ?????????(oldPage)?????? ???????????????.
            https://dogrushdev.tistory.com/239
         */
        translateLeftIn = AnimationUtils.loadAnimation(this, R.anim.translate_left_in);
        translateRightIn = AnimationUtils.loadAnimation(this, R.anim.translate_right_in);
        translateLeftOut = AnimationUtils.loadAnimation(this, R.anim.translate_left_out);
        translateRightOut = AnimationUtils.loadAnimation(this, R.anim.translate_right_out);

        translateRightOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /*
                for(int i=0; i<MAX_PAGES; i++){
                    if (currentPage!=i){
                        main_pages[i].setVisibility(View.INVISIBLE);
                    }
                }

                 */
                main_pages[previousPage].setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        translateLeftOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /*
                for(int i=0; i<MAX_PAGES; i++){
                    if (currentPage!=i){
                        main_pages[i].setVisibility(View.INVISIBLE);
                    }
                }

                 */
                main_pages[previousPage].setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        ((EditText)findViewById(R.id.page_2_lat)).setEnabled(false);
        ((EditText)findViewById(R.id.page_2_lng)).setEnabled(false);


        /*
            ???????????? ??????/????????? ????????? ????????? ???????????? ???????????? ???????????? ?????????????????????.
         */
        SeekBar seekBarMap = findViewById(R.id.seekbar_map);
        seekBarMap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (zoomInControl){
                    zoom = ZOOM_WEIGHT+i/10;
                    map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                zoomInControl = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                zoomInControl = false;
            }
        });
        /*
            ????????? ?????? ??? ???????????? ?????? ??????????????????.
            https://stackoverflow.com/questions/2013443/on-zoom-event-for-google-maps-on-android
         */
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                zoom = map.getCameraPosition().zoom - ZOOM_WEIGHT;
                seekBarMap.setProgress((int) zoom*10);
            }
        });
        /*
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(zoomInControl) zoomInControl = false;
            }
        });
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                int newZoom = 15;
                map.moveCamera(CameraUpdateFactory.zoomTo(newZoom));
                seekBarMap.setProgress(newZoom-ZOOM_WEIGHT);
                zoomInControl = true;
                return false;
            }
        });

         */
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(@NonNull LatLng point) {
                Double latitude = point.latitude;//Double.parseDouble(String.format("%.6f",point.latitude));
                Double longitude = point.longitude;//Double.parseDouble(String.format("%.6f",point.longitude));

                BitmapDescriptor bitmap = mMarker.bitmapDescriptorFromVector(MainActivity.this, R.drawable.view_card_marker);
                selectedMarker = map.addMarker(new MarkerOptions()
                        //.alpha(0.0f)
                        //.infoWindowAnchor(.5f, 1.0f)
                        .icon(bitmap)
                        .title(getString(R.string.click_to_add_new_marker))
                        .snippet(String.format("%.3f, %.3f",latitude,longitude))
                        .position(point));
                assert selectedMarker != null;
                selectedMarker.showInfoWindow();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 17));

                EditText editLat = findViewById(R.id.page_1_lat);
                EditText editLng = findViewById(R.id.page_1_lng);
                editLat.setText(String.valueOf(latitude));
                editLng.setText(String.valueOf(longitude));
            }
        });
        map.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(@NonNull Marker marker) {
                if (selectedMarker != null){
                    selectedMarker.remove();
                    selectedMarker = null;
                }
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if (mMarker.isSamePosition(marker, selectedMarker)){
                    movePage(0,1);
                } else {
                    double lat = marker.getPosition().latitude;
                    double lng = marker.getPosition().longitude;
                    String name = marker.getTitle();
                    String desc = marker.getSnippet();

                    EditText latEdit = findViewById(R.id.page_2_lat);
                    EditText lngEdit = findViewById(R.id.page_2_lng);
                    EditText nameEdit = findViewById(R.id.page_2_name);
                    EditText descEdit = findViewById(R.id.page_2_description);

                    latEdit.setText(String.valueOf(lat));
                    lngEdit.setText(String.valueOf(lng));
                    nameEdit.setText(name);
                    descEdit.setText(desc);

                    movePage(0,2);

                }

            }
        });


        reloadAllMarkers();

    }


    View.OnClickListener mOnclickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            EditText latEdit, lngEdit, nameEdit, descriptionEdit;
            String lat, lng, name, description;

            switch (view.getId()){
                case R.id.page_1_back_to_main:
                case R.id.page_2_back_to_main:
                    movePage(currentPage,0);
                    break;

                case R.id.page_1_add_marker:
                    latEdit = findViewById(R.id.page_1_lat);
                    lngEdit = findViewById(R.id.page_1_lng);
                    nameEdit = findViewById(R.id.page_1_name);
                    descriptionEdit = findViewById(R.id.page_1_description);
                    lat = latEdit.getText().toString();
                    lng = lngEdit.getText().toString();
                    name = nameEdit.getText().toString();
                    description = descriptionEdit.getText().toString();

                    if (lat.length() == 0 || lng.length() == 0){
                        showToast("????????? ????????? ??????????????????.");
                    } else if (name.length() == 0){
                        showToast("????????? ????????? ??????????????????.");
                    } else if (name.length() > 20){
                        showToast("????????? ????????? ?????? 20????????????.");
                    } else if (description.length() > 200){
                        showToast("????????? ????????? ?????? 200????????????.");
                    } else {
                        hideSoftKeyboard(MainActivity.this, view);
                        if (description.length() == 0){
                            description = "????????? ??????????????????";
                        }
                        double latDouble = Double.parseDouble(lat);
                        double lngDouble = Double.parseDouble(lng);
                        TextView test = findViewById(R.id.test);
                        if (createNewMarker(latDouble, lngDouble, name, description)){
                            showToast("????????? ?????????????????????.");
                            movePage(1,0);
                            latEdit.setText("");
                            lngEdit.setText("");
                            nameEdit.setText("");
                            descriptionEdit.setText("");
                        } else {
                            showToast("???????????? ???????????? ?????? ???????????????.");
                        }
                    }
                    break;

                case R.id.page_1_reload_markers:
                    reloadAllMarkers();
                    break;

                case R.id.page_1_reset_all_markers:
                    resetAllMarkers();
                    break;


                case R.id.page_2_save_changes:
                    latEdit = findViewById(R.id.page_2_lat);
                    lngEdit = findViewById(R.id.page_2_lng);
                    nameEdit = findViewById(R.id.page_2_name);
                    descriptionEdit = findViewById(R.id.page_2_description);
                    lat = latEdit.getText().toString();
                    lng = lngEdit.getText().toString();
                    name = nameEdit.getText().toString();
                    description = descriptionEdit.getText().toString();

                    if (lat.length() == 0 || lng.length() == 0){
                        showToast("???????????? ????????? ??????????????????.");
                    } else if (name.length() == 0) {
                        showToast("????????? ????????? ??????????????????.");
                    } else {
                        if (description.length() == 0){
                            description = "????????? ??????????????????";
                        }
                        hideSoftKeyboard(MainActivity.this, view);
                        double latDouble = Double.parseDouble(lat);
                        double lngDouble = Double.parseDouble(lng);
                        LatLng loc = new LatLng(latDouble, lngDouble);
                        if (editMarker(loc, name, description)){
                            //reloadAllMarkers();
                            movePage(1,0);
                            latEdit.setText("");
                            lngEdit.setText("");
                            nameEdit.setText("");
                            descriptionEdit.setText("");
                        } else {
                            showToast("?????? ????????? ??????????????????.");
                        }
                    }
                    break;

                case R.id.page_2_delete_marker:
                    latEdit = findViewById(R.id.page_2_lat);
                    lngEdit = findViewById(R.id.page_2_lng);
                    lat = latEdit.getText().toString();
                    lng = lngEdit.getText().toString();

                    if (lat.length() == 0 || lng.length() == 0){
                        showToast("???????????? ????????? ??????????????????.");
                    } else {
                        hideSoftKeyboard(MainActivity.this, view);
                        double latDouble = Double.parseDouble(lat);
                        double lngDouble = Double.parseDouble(lng);
                        LatLng loc = new LatLng(latDouble, lngDouble);
                        mMarker marker = com.example.mapwithmarker.mMarker.findMarkerWithPosition(loc);
                        marker.delete();
                        showToast("????????? ?????????????????????.");
                        movePage(2,0);
                    }

                break;

                case R.id.page_2_share_marker:
                    latEdit = findViewById(R.id.page_2_lat);
                    lngEdit = findViewById(R.id.page_2_lng);
                    nameEdit = findViewById(R.id.page_2_name);
                    descriptionEdit = findViewById(R.id.page_2_description);
                    lat = latEdit.getText().toString();
                    lng = lngEdit.getText().toString();
                    name = nameEdit.getText().toString();
                    description = descriptionEdit.getText().toString();

                    if (lat.length() == 0 || lng.length() == 0){
                        showToast("???????????? ????????? ??????????????????.");
                    } else {
                        Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                        Sharing_intent.setType("text/plain");

                        String messageToShare = "?????? ?????? ?????????\n??????: "+name+"\n??????: "+description+"\n??????: "+lat+"\n??????: "+lng+"\ntodo: ?????? ????????? ??????, ???????????? ?????? ?????? ?????? or ????????? ?????????????????? ??????";

                        Sharing_intent.putExtra(Intent.EXTRA_TEXT, messageToShare);

                        Intent Sharing = Intent.createChooser(Sharing_intent, "????????????");
                        startActivity(Sharing);
                    }
                    break;

            }
        }
    };

    /*
        oldPage: ?????? ?????????, ????????????????????? ????????? ??????????????? ??????????????????
        newPage: ????????? ?????????, ????????????????????? ????????? ???????????? ??????????????????.
     */
    public void movePage(int oldPage, int newPage){

        // ?????? ?????? ???????????? ??????????????? ?????????.
        for(int i=0; i<MAX_PAGES; i++){
            main_pages[i].setVisibility(View.INVISIBLE);
        }

        // ????????? ????????? ????????? ?????? ??? ???????????? ????????? ?????? ?????????????????? ??????????????????
        if (oldPage != newPage){
            currentPage = newPage;
            previousPage = oldPage;
            main_pages[oldPage].setVisibility(View.VISIBLE);
            main_pages[newPage].setVisibility(View.VISIBLE);

            // ????????? ????????? ?????? ???????????? ??????????????? ???????????? ????????? ???????????? ??????????????? ????????? ???????????????.
            if (newPage > oldPage){
                main_pages[oldPage].startAnimation(translateLeftOut);
                main_pages[newPage].startAnimation(translateLeftIn);
            } else {
                main_pages[oldPage].startAnimation(translateRightOut);
                main_pages[newPage].startAnimation(translateRightIn);
            }

        } else {
            // ????????? ????????? ???????????? ?????? ???????????? ????????? ?????? ??????????????????.
            main_pages[newPage].setVisibility(View.VISIBLE);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
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
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    // [START maps_current_place_show_current_place]
    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        MainActivity.this.openPlacesDialog();
                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }
    // [END maps_current_place_show_current_place]

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    // [START maps_current_place_open_places_dialog]
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];
                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                map.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }
    // [END maps_current_place_open_places_dialog]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
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

    /*
        ??????, ?????? ????????? ????????? ????????? ?????? ????????????, DB??? ???????????????.
        ????????? ????????? ?????? ?????? ????????? ?????? ????????? ???????????? ?????? ????????? ???????????????.
        (????????? ?????? ????????? ?????? ????????? ??????, Database.java ????????? ?????? ??????????????? ??? ??? ????????????)
     */
    boolean createNewMarker(double lat, double lng, String name, String description){
        LatLng loc = new LatLng(lat, lng);
        mMarker marker = new mMarker(MainActivity.this, loc, map);
        marker.setName(name);
        marker.setDescription(description);
        if (marker.addToDatebase()){
            marker.addToMap(true);
            marker.focus(15);
            return true;
        }
        else {
            return false;
        }
    }

    /*
        DB??? ????????? ?????? ?????? ???????????? ????????????. ???????????? ??????????????????.
     */
    void resetAllMarkers(){
        if (selectedMarker != null) selectedMarker.hideInfoWindow();
        map.clear();
        Database db = new Database(this);
        db.execSQL("DELETE FROM "+DB_TABLE_NAME);
        db.close();
        showToast("?????? ????????? ??????????????????.");
        //reloadAllMarkers();
    }

    /*
        ?????? ?????? ???, ????????? ??????????????? ????????? ??? ???, DB?????? ????????? ????????? mMarker ????????? ????????????
        ?????? ???????????? ????????? ?????????.
     */
    void reloadAllMarkers(){
        //TextView result = new TextView(this);
        Database db = new Database(this);
        double lat, lng;
        LatLng loc;
        mMarker marker;
        String name, desc;

        if (selectedMarker != null) selectedMarker.hideInfoWindow();
        map.clear();
        Cursor cursor = db.querySQL("SELECT * FROM "+DB_TABLE_NAME);
        if (cursor.getCount() != 0){
            TextView test = findViewById(R.id.test);
            test.setText("");
            while (cursor.moveToNext()){
                lat = Double.parseDouble(cursor.getString(0));
                lng = Double.parseDouble(cursor.getString(1));
                name = cursor.getString(2);
                desc = cursor.getString(3);
                loc = new LatLng(lat,lng);
                test.append(String.format("\nlat: %f, lng: %f, name: %s, desc: %s",lat,lng,name,desc));
                test.append(cursor.getString(0)+", "+cursor.getString(1));
                marker = new mMarker(MainActivity.this, loc, map);
                marker.setName(name);
                marker.setDescription(desc);
                marker.addToMap(false);
                //marker.addToDatebase();
            }
        }
        showToast("?????? ????????? ??????????????????.");

        db.close();
    }

    @Deprecated
    boolean editMarker(Marker m, String name, String description){

        LatLng loc = m.getPosition();
        mMarker marker = com.example.mapwithmarker.mMarker.findMarkerWithPosition(loc);

        if (marker != null){
            marker.setName(name);
            marker.setDescription(description);
            marker.refresh(false);

            return true;
        } else {
            return false;
        }
    }
    boolean editMarker(LatLng loc, String name, String description){

        mMarker marker = com.example.mapwithmarker.mMarker.findMarkerWithPosition(loc);

        if (marker != null){
            marker.setName(name);
            marker.setDescription(description);
            marker.refresh(false);

            return true;
        } else {
            return false;
        }
    }

    /*
        ????????? ?????? ????????? ????????? Toast.makeText??? ?????? ???????????? ??????????????????.
        showToast("Hello world"); ?????? ???????????? ???????????? ????????? ?????? ??? ????????????.
     */
    void showToast(String msg){
        Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    /*
        EditText ?????? ??? ????????? ????????? ???????????? ????????? ????????? ?????????.
        ???????????? ????????? ???????????? ????????? ?????????.
        hideSoftKeyborad(MainActivity.this, view);
     */
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

}






