package com.seyoung.pettourservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;

//import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class TourType extends AppCompatActivity {

    MapView mapView;
    KakaoMap kakaoMap;
    FusedLocationProviderClient fusedLocationClient;
//    TextInputLayout textInputLayout;
//    AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour_type);

        int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission1 == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            }
            return;
        }

        mapView = findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                Log.d("kakao Map", " onMapDestroy");
            }

            @Override
            public void onMapError(Exception error) {
                Log.d("kakao Map", " onMapError" + error.getMessage());
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
            }
        });



        // 데이터 리스트
        String[] dropdownItems = {"Home", "Work", "Other", "Custom"};

        // Spinner와 연결
        Spinner customSpinner = findViewById(R.id.adminArea);

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                dropdownItems
        );

        // 드롭다운 뷰 설정
        adapter.setDropDownViewResource(R.layout.item_list);

        // Spinner에 어댑터 연결
        customSpinner.setAdapter(adapter);


        String[] dropdownItems2 = {"Home", "Work", "Other", "Custom", "Work", "Other", "Custom", "Work", "Other", "Custom"};

        // Spinner와 연결
        Spinner customSpinner2 = findViewById(R.id.subLocality);

        // 어댑터 설정
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                dropdownItems2
        );

        // 드롭다운 뷰 설정
        adapter2.setDropDownViewResource(R.layout.item_list);

        // Spinner에 어댑터 연결
        customSpinner2.setAdapter(adapter2);



        // 데이터 리스트
        String[] dropdownItems3 = {"Home", "Work", "Other", "Custom"};

        // Spinner와 연결
        Spinner customSpinner3 = findViewById(R.id.depth1);

        // 어댑터 설정
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                dropdownItems3
        );

        // 드롭다운 뷰 설정
        adapter3.setDropDownViewResource(R.layout.item_list);

        // Spinner에 어댑터 연결
        customSpinner3.setAdapter(adapter3);




        // 데이터 리스트
        String[] dropdownItems4 = {"Home", "Work", "Other", "Custom"};

        // Spinner와 연결
        Spinner customSpinner4 = findViewById(R.id.depth2);

        // 어댑터 설정
        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                dropdownItems4
        );

        // 드롭다운 뷰 설정
        adapter4.setDropDownViewResource(R.layout.item_list);

        // Spinner에 어댑터 연결
        customSpinner4.setAdapter(adapter4);


//        textInputLayout = findViewById(R.id.inputLayout);
//        autoCompleteTextView = findViewById(R.id.text_item);
//
//        String[] items = {"경기도", "충청남도", "item3", "item4", "item5"};
//        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(TourType.this, R.layout.item_list, items);
//        autoCompleteTextView.setAdapter(itemAdapter);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


    }
}
