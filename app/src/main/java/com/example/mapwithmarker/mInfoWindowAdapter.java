package com.example.mapwithmarker;


import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class mInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final MainActivity mainActivity;

    public mInfoWindowAdapter(MainActivity mActivity){
        mainActivity = mActivity;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;

        LinearLayout layout = (LinearLayout) View.inflate(mainActivity, R.layout.marker_popup, null);
        TextView latLngView = layout.findViewById(R.id.txt_latLng_markerPopup);
        String latLng = "위도:"+lat+",\n경도:"+lng;
        latLngView.setText(latLng);

        /*
        TextView titleView = layout.findViewById(R.id.txt_title_markerPopup);
        String title = "마커 이름";
        titleView.setText(title);

         */

        return layout;
    }
}