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

    final int ZOOM_WEIGHT = 8;
    int zoom = ZOOM_WEIGHT+5;
    boolean zoomInControl = false;


    //Server server;
    //final String PHP_SERVER_URL = "http://121.124.124.95/PHP_connection.php";

    Button btn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            내비게이션 색상 변경 (build.gradle (:app)에서 최소 api 레벨을 21이상으로 바꿔야함)
            아래 링크를 참조하였습니다.
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

        btn1 = findViewById(R.id.btn1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "공유할 Text";

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing = Intent.createChooser(Sharing_intent, "공유하기");
                startActivity(Sharing);
            }
        });

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
            확대&축소 버튼 추가. 아래 링크를 참조하였습니다.
            https://developers.google.com/maps/documentation/android-sdk/controls?hl=ko#zoom_controls

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

         */

        /*
            커스텀 마커입니다. mMarker 클래스를 따로 만들어 위치만 지정해주면 마커를 알아서 생성해줍니다.
            객체 생성시 매개 변수로는 [현재 Activity, 위치(LatLng), 지도]가 들어갑니다.
         */
        LatLng loc = new LatLng(36.99502, 127.13327);
        //mMarker marker = new mMarker(MainActivity.this, loc, googleMap);
        //marker.focus(18);

        /*
            커스텀 마커 클릭 시 뜨는 메시지의 어댑터를 설정합니다.
         */
        map.setInfoWindowAdapter(new mInfoWindowAdapter(MainActivity.this));

        /*
            지도의 최대/최소 줌 레벨을 결정합니다.
         */
        map.setMinZoomPreference(8);
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
        리스너 등록 등 원래 onCreate에서 수행됐어야 하는 것들을 이곳에서 따로 처리합니다.
        이것이 따로 메소드로 만들어져 onMapReady에 있는 이유는, mapFragment.getMapAsync와 함께 실행하면
        충돌이 발생하기 때문입니다.
     */
    private void onActivityReady() {

        /*
            원래는 FrameLayout에 버튼과 이미지+텍스트를 표시하는 레이아웃을 중첩시켰지만,
            Button 대신 LinearLayout에서 clickable 속성을 true로 해준 뒤, 클릭 리스너를 등록시켰습니다.
         */
        findViewById(R.id.btn_moveTo_page_0).setOnClickListener(mOnclickListener);
        findViewById(R.id.btn_moveTo_page_1).setOnClickListener(mOnclickListener);
        findViewById(R.id.btn_moveTo_page_2).setOnClickListener(mOnclickListener);
        //findViewById(R.id.btn_getServerData).setOnClickListener(mOnclickListener);

        colPrimary = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);
        colAccent = ContextCompat.getColor(MainActivity.this, R.color.colorAccent);


        /*
            main_pages: FrameLayout에 들어갈 각 페이지들을 리스트에 담아둡니다.
            예를 들어, main_pages[1]은 페이지 1, 즉 두번째 페이지입니다.
            페이지간 이동할 땐 movePage() 메소드를 사용하면 애니메이션을 포함한 페이지 이동이 자유롭게 가능합니다.
            하단 바의 버튼 클릭으로 가능한 페이지 이동뿐만 아니라 마커추가 등 기타 페이지도 추가할 수 있습니다.
         */
        main_pages = new LinearLayout[MAX_PAGES];
        main_pages[0] = (LinearLayout) findViewById(R.id.main_pages_0);
        main_pages[1] = (LinearLayout) findViewById(R.id.main_pages_1);
        main_pages[1].addView(View.inflate(this, R.layout.page_1, null));
        main_pages[2] = (LinearLayout) findViewById(R.id.main_pages_2);
        main_pages[2].addView(View.inflate(this, R.layout.page_2, null));
        /* 페이지 추가 코드 (n 대신 숫자)
        main_pages[n] = (LinearLayout) findViewById(R.id.main_pages_n);
        main_pages[n].addView(View.inflate(this, R.layout.page_n, null));
         */

        findViewById(R.id.page_2_add_marker).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_2_reload_markers).setOnClickListener(mOnclickListener);
        findViewById(R.id.page_2_reset_all_markers).setOnClickListener(mOnclickListener);

        /*
            페이지 전환 애니메이션 로드 & 리스너 등록입니다. 아래 링크를 참고하였습니다.
            IN은 화면에 들어오는 페이지(newPage), OUT은 화면을 나가는 페이지(oldPage)에게 적용됩니다.
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

        /*
            간편하게 확대/축소가 가능한 시크바 리스너를 등록하여 카메라를 변경시킵니ㅏㄷ.
         */
        SeekBar seekBarMap = findViewById(R.id.seekbar_map);
        seekBarMap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                zoom = ZOOM_WEIGHT+i;
                map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
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
            카메라 이동 시 시크바도 함께 이동시킵니다.
            https://stackoverflow.com/questions/2013443/on-zoom-event-for-google-maps-on-android
         */
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (!zoomInControl){
                    zoom = (int)map.getCameraPosition().zoom - ZOOM_WEIGHT;
                    seekBarMap.setProgress(zoom);
                }
            }
        });
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                int newZoom = 13;
                map.moveCamera(CameraUpdateFactory.zoomTo(newZoom));
                seekBarMap.setProgress(newZoom-ZOOM_WEIGHT);
                return false;
            }
        });

        reloadAllMarkers();

    }

    View.OnClickListener mOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_moveTo_page_0:
                    view.setBackgroundColor(colAccent);
                    movePage(currentPage,0);
                    findViewById(R.id.btn_moveTo_page_1).setBackgroundColor(colPrimary);
                    findViewById(R.id.btn_moveTo_page_2).setBackgroundColor(colPrimary);
                    break;

                case R.id.btn_moveTo_page_1:
                    view.setBackgroundColor(colAccent);
                    movePage(currentPage,1);
                    findViewById(R.id.btn_moveTo_page_0).setBackgroundColor(colPrimary);
                    findViewById(R.id.btn_moveTo_page_2).setBackgroundColor(colPrimary);
                    break;

                case R.id.btn_moveTo_page_2:
                    view.setBackgroundColor(colAccent);
                    movePage(currentPage,2);
                    findViewById(R.id.btn_moveTo_page_1).setBackgroundColor(colPrimary);
                    findViewById(R.id.btn_moveTo_page_0).setBackgroundColor(colPrimary);
                    break;

                case R.id.page_2_add_marker:
                    EditText editLat = (EditText)findViewById(R.id.page_2_lat);
                    EditText editLng = (EditText)findViewById(R.id.page_2_lng);
                    String latStr = editLat.getText().toString();
                    String lngStr = editLng.getText().toString();

                    if (latStr.length() != 0 && lngStr.length() != 0){
                        hideSoftKeyboard(MainActivity.this, view);
                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);
                        createNewMarker(lat, lng);
                        showToast("마커가 추가되었습니다.");
                        movePage(2,0);
                        editLat.setText("");
                        editLng.setText("");

                    } else {
                        showToast("위도와 경도를 입력해주세요.");
                    }
                    break;

                case R.id.page_2_reload_markers:
                    reloadAllMarkers();
                    break;

                case R.id.page_2_reset_all_markers:
                    resetAllMarkers();
                    break;
            }
        }
    };

    /*
        oldPage: 이전 페이지, 애니메이션에서 밖으로 밀려나가는 페이지입니다
        newPage: 이동할 페이지, 애니메이션에서 안으로 들어오는 페이지입니다.
     */
    public void movePage(int oldPage, int newPage){

        // 먼저 모든 페이지를 안보이도록 합니다.
        for(int i=0; i<MAX_PAGES; i++){
            main_pages[i].setVisibility(View.INVISIBLE);
        }

        // 페이지 이동이 필요한 경우 두 페이지를 보이게 하고 애니메이션을 작동시킵니다
        if (oldPage != newPage){
            currentPage = newPage;
            previousPage = oldPage;
            main_pages[oldPage].setVisibility(View.VISIBLE);
            main_pages[newPage].setVisibility(View.VISIBLE);

            // 페이지 번호에 따라 왼쪽에서 오른쪽으로 정렬되어 있다고 가정하여 애니메이션 방향을 결정합니다.
            if (newPage > oldPage){
                main_pages[oldPage].startAnimation(translateLeftOut);
                main_pages[newPage].startAnimation(translateLeftIn);
            } else {
                main_pages[oldPage].startAnimation(translateRightOut);
                main_pages[newPage].startAnimation(translateRightIn);
            }

            if (newPage <= 3){
                findViewById(R.id.btn_moveTo_page_0).setBackgroundColor(colPrimary);
                findViewById(R.id.btn_moveTo_page_1).setBackgroundColor(colPrimary);
                findViewById(R.id.btn_moveTo_page_2).setBackgroundColor(colPrimary);
                switch (newPage){
                    case 1:
                        findViewById(R.id.btn_moveTo_page_1).setBackgroundColor(colAccent);
                        break;
                    case 2:
                        findViewById(R.id.btn_moveTo_page_2).setBackgroundColor(colAccent);
                        break;
                    default:
                        findViewById(R.id.btn_moveTo_page_0).setBackgroundColor(colAccent);
                }
            }

        } else {
            // 페이지 이동이 없으므로 현재 페이지를 그대로 다시 표시시킵니다.
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
        위도, 경도 정보를 가지고 마커를 맵에 추가하고, DB에 기록합니다.
        맵에서 마커를 찍는 것이 된다면 기타 정보도 입력하기 위해 수정이 필요합니다.
        (데이터 중복 체크는 아직 미완성 단계, Database.java 쪽에서 다시 건들어봐야 될 것 같습니다)
     */
    void createNewMarker(double lat, double lng){
        LatLng loc = new LatLng(lat, lng);
        mMarker marker = new mMarker(MainActivity.this, loc, map);
        if (!marker.addToDatebase()){
            showToast("중복되는 데이터가 이미 DB에 존재합니다");
        }
        marker.focus(15);
    }

    /*
        DB에 저장된 모든 마커 데이터를 지웁니다. 디버깅용 메소드입니다.
     */
    void resetAllMarkers(){
        map.clear();
        Database db = new Database(this);
        db.execSQL("DELETE FROM "+DB_TABLE_NAME);
        db.close();
        showToast("모든 마커를 삭제했습니다.");
        //reloadAllMarkers();
    }

    /*
        앱이 켜질 때, 마커가 추가되거나 삭제될 때 등, DB에서 정보를 불러와 mMarker 객체를 생성하여
        맵에 추가하는 역할을 합니다.
     */
    void reloadAllMarkers(){
        //TextView result = new TextView(this);
        Database db = new Database(this);
        double lat, lng;
        LatLng loc;
        mMarker marker;

        map.clear();
        Cursor cursor = db.querySQL("SELECT * FROM "+DB_TABLE_NAME);
        if (cursor.getCount() != 0){
            while (cursor.moveToNext()){
                lat = Double.parseDouble(cursor.getString(0));
                lng = Double.parseDouble(cursor.getString(1));
                loc = new LatLng(lat,lng);
                marker = new mMarker(MainActivity.this, loc, map);
                marker.addToDatebase();
            }
        }
        showToast("모든 마커를 불러왔습니다.");

        db.close();
    }

    /*
        메시지 등을 띄울때 일일히 Toast.makeText를 하기 귀찮아서 만들었습니다.
        showToast("Hello world"); 같은 방식으로 간단하게 토스트 띄울 수 있습니다.
     */
    void showToast(String msg){
        Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    /*
        EditText 입력 후 버튼을 누르면 키보드를 내리는 역할을 합니다.
        아래처럼 버튼의 리스너에 넣으면 됩니다.
        hideSoftKeyborad(MainActivity.this, view);
     */
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}






