package com.example.mapwithmarker;


import static com.example.mapwithmarker.Database.DB_TABLE_NAME;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    public mMarker(MainActivity mActivity, LatLng loc, GoogleMap googleMap) {
        mainActivity = mActivity;
        latLng = loc;
        lat = latLng.latitude;
        lng = latLng.longitude;

        map = googleMap;
        this.addToMap();
        //this.addToDatebase();
    }

    /*
        이 메소드는 처음 계획시 add(googleMap)으로 하려고 하였으나,
        내부 메소드로 바꿔 객체 생성 시 자동으로 구글맵에 마커를 추가시켜주도록 하였습니다.
     */
    private void addToMap(){
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Custom marker")
                .snippet("위도:"+lat+", 경도:"+lng)
                .icon(bitmapDescriptorFromVector(mainActivity, R.drawable.icon_marker_1)));

    }

    /*
        마커를 DB에 실제로 넣을 때 사용하는 메소드입니다.
     */
    public boolean addToDatebase(){
        Database db = new Database(mainActivity);
        Cursor cursor = db.querySQL("SELECT * FROM "+DB_TABLE_NAME+" WHERE lat="+lat+" AND lng="+lng);
        // 중복 데이터 방지를 위해 조건문을 넣었으나 작동 안됨. 수정 필요
        if (cursor.getCount() == 0){
            db.execSQL(String.format(
                    "INSERT INTO "+DB_TABLE_NAME+" VALUES("+lat+","+lng+");"
            ));
            db.close();
            return true;
        }
        db.close();
        return false;
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
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
