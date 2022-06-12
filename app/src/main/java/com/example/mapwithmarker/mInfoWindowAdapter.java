package com.example.mapwithmarker;


import android.annotation.SuppressLint;
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

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        double lat = marker.getPosition().latitude;
        double lng = marker.getPosition().longitude;

        LinearLayout layout = (LinearLayout) View.inflate(mainActivity, R.layout.marker_popup, null);

        TextView descText = layout.findViewById(R.id.txt_description_markerPopup);
        String snippet = marker.getSnippet();
        descText.setText(snippet);

        TextView titleText = layout.findViewById(R.id.txt_title_markerPopup);
        String name = marker.getTitle();
        titleText.setText(name);

        return layout;
    }
}