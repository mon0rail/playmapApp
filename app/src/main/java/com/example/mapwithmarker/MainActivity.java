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

import static com.google.android.gms.maps.UiSettings.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    int colPrimary, colAccent, currentPage=0, previousPage;
    final int MAX_PAGES = 3;
    LinearLayout[] main_pages;
    Animation translateLeftIn, translateRightIn, translateLeftOut, translateRightOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
            내비게이션 색상 변경 (build.gradle (:app)에서 최소 api 레벨을 21이상으로 바꿔야함)
            아래 링크를 참조하였습니다.
            https://calvinjmkim.tistory.com/9
            https://figureking.tistory.com/351
         */
        int colNavi = ContextCompat.getColor(MainActivity.this, R.color.colorAccent);
        getWindow().setNavigationBarColor(colNavi);

        setContentView(R.layout.layout_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*
            확대&축소 버튼 추가. 아래 링크를 참조하였습니다.
            https://developers.google.com/maps/documentation/android-sdk/controls?hl=ko#zoom_controls
         */
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        /*
            커스텀 마커입니다. mMarker 클래스를 따로 만들어 위치만 지정해주면 마커를 알아서 생성해줍니다.
            객체 생성시 매개 변수로는 [현재 Activity, 위치(LatLng), 지도]가 들어갑니다.
         */
        LatLng loc = new LatLng(36.99502, 127.13327);
        mMarker marker = new mMarker(MainActivity.this, loc, googleMap);
        marker.focus(18);

        onActivityReady();
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
        findViewById(R.id.btn_moveTo_page_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(colAccent);
                //Toast.makeText(MainActivity.this, "Button 1 Clicked", Toast.LENGTH_SHORT).show();
                movePage(currentPage,0);
                findViewById(R.id.btn_moveTo_page_1).setBackgroundColor(colPrimary);
                findViewById(R.id.btn_moveTo_page_2).setBackgroundColor(colPrimary);
            }
        });
        findViewById(R.id.btn_moveTo_page_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(colAccent);
                //Toast.makeText(MainActivity.this, "Button 2 Clicked", Toast.LENGTH_SHORT).show();
                movePage(currentPage,1);
                findViewById(R.id.btn_moveTo_page_0).setBackgroundColor(colPrimary);
                findViewById(R.id.btn_moveTo_page_2).setBackgroundColor(colPrimary);
            }
        });
        findViewById(R.id.btn_moveTo_page_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(colAccent);
                //Toast.makeText(MainActivity.this, "Button 2 Clicked", Toast.LENGTH_SHORT).show();
                movePage(currentPage,2);
                findViewById(R.id.btn_moveTo_page_1).setBackgroundColor(colPrimary);
                findViewById(R.id.btn_moveTo_page_0).setBackgroundColor(colPrimary);
            }
        });

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
        main_pages[2] = (LinearLayout) findViewById(R.id.main_pages_2);

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

    }

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
        } else {
            // 페이지 이동이 없으므로 현재 페이지를 그대로 다시 표시시킵니다.
            main_pages[newPage].setVisibility(View.VISIBLE);
        }
    }


}
