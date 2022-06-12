package com.example.mapwithmarker;


import static com.example.mapwithmarker.Database.DB_TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;

/*
    처음에는 Marker를 상속하여 만들고자 했으나, Marker가 final이라서 상속이 불가능했습니다.
    그래서 mMarker는 실질적으로 마커 객체가 되는 것은 아니지만, 마커를 대신 관리해주는 클래스라고 볼 수 있습니다.
 */
public class mMarker{

    private final LatLng latLng;
    private final MainActivity mainActivity;
    private final GoogleMap map;
    private final double lat;
    private final double lng;
    private String name;
    private String description;
    Marker marker = null;

    static ArrayList<mMarker> MARKER_LIST = new ArrayList<>();

    public mMarker(MainActivity mActivity, LatLng loc, GoogleMap googleMap) {
        mainActivity = mActivity;
        name = mainActivity.getString(R.string.marker_name_empty);
        description = mainActivity.getString(R.string.marker_desc_empty);
        latLng = loc;
        lat = latLng.latitude;
        lng = latLng.longitude;

        map = googleMap;
        //this.addToMap(false);
        MARKER_LIST.add(this);
    }


    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    static public boolean isSamePosition(LatLng loc1, LatLng loc2) {

        if (loc1 != null && loc2 != null) {
            double lat1 = loc1.latitude;
            double lng1 = loc1.longitude;
            double lat2 = loc2.latitude;
            double lng2 = loc2.longitude;
            return lat1 == lat2 && lng1 == lng2;
        }
        return false;
    }

    /*
        setName(), setDescription() 등을 실행한 뒤에 실행하는 메소드입니다.
        마커를 맵에 추가하는 역할을 하며, DB에 등록시키려면 addToDatabase()도 같이 실행하면 됩니다.
     */
    public void addToMap(boolean showInfoWindow){
        if (marker != null) {
            marker.remove();
        }
        marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(name)
                .snippet(description)
                .icon(bitmapDescriptorFromVector(mainActivity, R.drawable.icon_marker_1)));
        if (showInfoWindow) {
            assert marker != null;
            marker.showInfoWindow();
        }




        /*
        Button btn = layout.findViewById(R.id.btn_share_marker_popup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("text/plain");

                String Test_Message = "마커 좌표:"+lat+", "+lng;

                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                Intent Sharing = Intent.createChooser(Sharing_intent, "공유하기");
                mainActivity.startActivity(Sharing);
            }
        });

         */

    }
    public void refresh(boolean showInfoWindow){
        //addToMap(showInfoWindow);
        addToDatebase();
    }

    /*
        마커를 DB에 실제로 넣을 때 사용하는 메소드입니다.
     */
    public boolean addToDatebase(){
        Database db = new Database(mainActivity);

        Cursor cursor;
        String sql, sqlInsert;

        sql = "SELECT * FROM "+DB_TABLE_NAME;
        cursor = db.querySQL(sql);
        ArrayList<mMarker> list = new ArrayList<>();

        while (cursor.moveToNext()) {
            double lat = Double.parseDouble(cursor.getString(0));
            double lng = Double.parseDouble(cursor.getString(1));
            if (this.lat != lat && this.lng != lng){
                String name = cursor.getString(2);
                String desc = cursor.getString(3);
                LatLng loc = new LatLng(lat, lng);
                mMarker m = new mMarker(mainActivity, loc, map);
                m.setName(name);
                m.setDescription(desc);
                list.add(m);
            }
            //m.addToMap(false);
        }
        list.add(mMarker.this);

        db.execSQL("DELETE FROM "+DB_TABLE_NAME);

        for (mMarker m : list){
            m.addToMap(false);
            double lat = m.lat;
            double lng = m.lng;
            String name = m.name;
            String description = m.description;
            sqlInsert = "INSERT INTO "+DB_TABLE_NAME+" VALUES('"+lat+"','"+lng+"','"+name+"','"+description+"');";
            db.execSQL(sqlInsert);
        }

        db.close();
        return true;
    }

    public void delete(){
        marker.remove();
        Database db = new Database(mainActivity);

        Cursor cursor;
        String sql, sqlInsert;

        sql = "SELECT * FROM "+DB_TABLE_NAME;
        cursor = db.querySQL(sql);
        ArrayList<mMarker> list = new ArrayList<>();

        while (cursor.moveToNext()) {
            double lat = Double.parseDouble(cursor.getString(0));
            double lng = Double.parseDouble(cursor.getString(1));
            if (this.lat != lat && this.lng != lng){
                String name = cursor.getString(2);
                String desc = cursor.getString(3);
                LatLng loc = new LatLng(lat, lng);
                mMarker m = new mMarker(mainActivity, loc, map);
                m.setName(name);
                m.setDescription(desc);
                list.add(m);
            }
            //m.addToMap(false);
        }

        db.execSQL("DELETE FROM "+DB_TABLE_NAME);

        for (mMarker m : list){
            m.addToMap(false);
            double lat = m.lat;
            double lng = m.lng;
            String name = m.name;
            String description = m.description;
            sqlInsert = "INSERT INTO "+DB_TABLE_NAME+" VALUES('"+lat+"','"+lng+"','"+name+"','"+description+"');";
            db.execSQL(sqlInsert);
        }

        db.close();
    }

    /*
        카메라를 마커로 이동하고 원하는대로 확대하는 메소드입니다. 아래 링크를 참고하였습니다.
        https://dev.eyegood.co.kr/entry/android-google-map-camera-move%EC%B9%B4%EB%A9%94%EB%9D%BC%EC%9D%B4%EB%8F%99
     */
    public void focus(int zoom){
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /*
        벡터 이미지를 마커 아이콘에 적용하기 위해 가져온 메소드입니다.
        https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
     */

    static public boolean isSamePosition(Marker marker1, Marker marker2) {

        if (marker1 != null && marker2 != null) {
            LatLng loc1 = marker1.getPosition();
            LatLng loc2 = marker2.getPosition();
            return isSamePosition(loc1, loc2);
        }
        return false;
    }
    static public BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    static public mMarker findMarkerWithPosition(LatLng loc){
        if (loc != null) {
            for (mMarker m : MARKER_LIST){
                if (m != null &&
                        m.lat == loc.latitude &&
                        m.lng == loc.longitude){
                    return m;
                }
            }
        }
        return null;
    }

}
