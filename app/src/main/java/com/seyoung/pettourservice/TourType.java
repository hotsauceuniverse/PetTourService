package com.seyoung.pettourservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

//import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TourType extends AppCompatActivity {

    MapView mapView;
    KakaoMap kakaoMap;
    FusedLocationProviderClient fusedLocationClient;
    SlidingUpPanelLayout Sliding;
    LinearLayout Drawer;
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

        Drawer = findViewById(R.id.drawer);
        Drawer.setOnClickListener(null);    // 클릭으로 열고 닫히는 기능 막기

        Sliding = findViewById(R.id.main_panel);

        // 데이터 리스트
        String[] dropdownItems = {"클릭 불가","Home", "Work", "Other", "Custom"};

        // Spinner와 연결
        Spinner customSpinner = findViewById(R.id.adminArea);

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                dropdownItems
        ) {
            @Override
            public boolean isEnabled(int position) {
                // 첫 번째 아이템을 선택할 수 없게 설정
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                // 첫 번째 아이템은 비활성화 (회색으로 표시)
                if (position == 0) {
                    tv.setTextColor(getColor(R.color.gray));
                } else {
                    tv.setTextColor(getColor(R.color.black));
                }
                return view;
            }
        };

        // 드롭다운 뷰 설정
        adapter.setDropDownViewResource(R.layout.item_list);

        // Spinner에 어댑터 연결
        customSpinner.setAdapter(adapter);

        // Spinner 선택 이벤트 리스너
        customSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // 첫 번째 아이템이 선택되었을 경우 아무 동작도 하지 않음
                    return;
                }
                // 유효한 아이템이 선택되었을 경우 처리
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.d("Spinner", "선택된 아이템: " + selectedItem);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String[] dropdownItems2 = {"Home", "Work", "Other", "Custom", "Work", "Other", "Custom", "Work", "Other", "Custom"};
        Spinner customSpinner2 = findViewById(R.id.subLocality);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this,
                R.layout.item_list,
                dropdownItems2
        );
        adapter2.setDropDownViewResource(R.layout.item_list);
        customSpinner2.setAdapter(adapter2);

        String[] dropdownItems3 = {"Home", "Work", "Other", "Custom"};
        Spinner customSpinner3 = findViewById(R.id.type_1);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(
                this,
                R.layout.item_list,
                dropdownItems3
        );
        adapter3.setDropDownViewResource(R.layout.item_list);
        customSpinner3.setAdapter(adapter3);

        String[] dropdownItems4 = {"Home", "Work", "Other", "Custom"};
        Spinner customSpinner4 = findViewById(R.id.type_2);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(
                this,
                R.layout.item_list,
                dropdownItems4
        );
        adapter4.setDropDownViewResource(R.layout.item_list);
        customSpinner4.setAdapter(adapter4);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new getAddressFromLocation().execute();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1000) {
//            boolean check_result = true;
//
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    check_result = false;
//                    break;
//                }
//            }
//
//            if (check_result == false) {
//                finish();
//            }
//        }
//    }


//    private void getAddressFromLocation() {
//        String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
//        String queryUrl = "https://apis.data.go.kr/B551011/KorPetTourService/areaCode?serviceKey="+ code +"&pageNo=1&numOfRows=20&MobileOS=ETC&MobileApp=AppTest";
//        Log.d("queryUrl   ", "queryUrl   " + queryUrl);
//    }

    public class getAddressFromLocation extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> areaNames = new ArrayList<>();
            try {
                String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
                String queryUrl = "https://apis.data.go.kr/B551011/KorPetTourService/areaCode?serviceKey="+ code +"&pageNo=1&numOfRows=20&MobileOS=ETC&MobileApp=AppTest";

                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                int eventType = parser.getEventType();
                String tagName;
                String areaName = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("name")) {
                            parser.next();
                            areaName = parser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("item") && areaName != null) {
                            areaNames.add(areaName);
                            areaName = null;
                        }
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return areaNames;
        }

        @Override
        protected void onPostExecute(List<String> areaNames) {
            super.onPostExecute(areaNames);
            if (!areaNames.isEmpty()) {
                setupSpinner(areaNames);
            }
        }
    }

    private void setupSpinner(List<String> areaNames) {
        // Spinner와 연결
        Spinner areaSpinner = findViewById(R.id.adminArea);

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                areaNames
        ) {
//            @Override
//            public boolean isEnabled(int position) {
//                // 첫 번째 아이템을 선택할 수 없게 설정
//                return position != 0;
//            }
//
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//
//                // 첫 번째 아이템은 비활성화 (회색으로 표시)
//                if (position == 0) {
//                    tv.setTextColor(getColor(R.color.gray));
//                } else {
//                    tv.setTextColor(getColor(R.color.black));
//                }
//                return view;
//            }
        };

        // 드롭다운 뷰 설정
        adapter.setDropDownViewResource(R.layout.item_list);

        // Spinner에 어댑터 연결
        areaSpinner.setAdapter(adapter);

        // Spinner 선택 이벤트 리스너
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // 첫 번째 아이템이 선택되었을 경우 아무 동작도 하지 않음
                    return;
                }
                // 유효한 아이템이 선택되었을 경우 처리
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.d("Spinner", "선택된 아이템: " + selectedItem);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
