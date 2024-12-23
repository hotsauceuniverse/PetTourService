package com.seyoung.pettourservice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TourType extends AppCompatActivity {

    MapView mapView;
    KakaoMap kakaoMap;
    FusedLocationProviderClient fusedLocationClient;
    LinearLayout Drawer;
    private String selectedAreaCode;    // 선택된 areaCode를 저장할 변수

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

        String[] dropdownItems3 = {"Home", "Work", "Other", "Custom"};
        Spinner customSpinner3 = findViewById(R.id.type_1);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(
                this,
                R.layout.item_list,
                dropdownItems3
        );
        adapter3.setDropDownViewResource(R.layout.item_list);
        customSpinner3.setAdapter(adapter3);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new getAreaFromLocation().execute();

        // 하위 지역 스피너 초기화 (더미 데이터 설정)
        List<String> dummySubLocality = new ArrayList<>();
        setupSubLocalitySpinner(dummySubLocality);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result == false) {
                finish();
            }
        }
    }

    public class getAreaFromLocation extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            Map<String, String> areaData = new LinkedHashMap<>(); // LinkedHashMap으로 순서 보장

            try {
                String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
                String queryUrl = "https://apis.data.go.kr/B551011/KorPetTourService/areaCode?serviceKey="
                        + code
                        + "&pageNo=1&numOfRows=20&MobileOS=ETC&MobileApp=AppTest";

                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                int eventType = parser.getEventType();
                String tagName;
                String name = null, codeValue = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("name")) {
                            parser.next();
                            name = parser.getText();
                        } else if (tagName.equals("code")) {
                            parser.next();
                            codeValue = parser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("item") && name != null && codeValue != null) {
                            areaData.put(name, codeValue);
                            name = null;
                            codeValue = null;
                        }
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return areaData;
        }

        @Override
        protected void onPostExecute(Map<String, String> areaData) {
            super.onPostExecute(areaData);
            if (!areaData.isEmpty()) {
                setupSpinner(areaData);
            }
        }
    }

    private void setupSpinner(Map<String, String> areaData) {
        // Spinner와 연결
        Spinner areaSpinner = findViewById(R.id.adminArea);
        List<String> areaNames = new ArrayList<>(areaData.keySet());
        areaNames.add(0, "도/시 선택");

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_list,  // 커스텀 레이아웃
                areaNames
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == 0) {
                    tv.setTextColor(getColor(R.color.gray));
                } else {
                    tv.setTextColor(getColor(R.color.black));
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.item_list);
        areaSpinner.setAdapter(adapter);

        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;

                String selectedName = areaNames.get(position);  // 실제 전달 해야 할 값 <code> = 31
                selectedAreaCode = areaData.get(selectedName);  // 선택된 이름으로 코드 조회
                Log.d("selectedAreaCode", "Selected Area Code: " + selectedAreaCode);

                new getsubLocalityFromLocation().execute(); // 하위 지역 데이터 로드
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public class getsubLocalityFromLocation extends AsyncTask<Void, Void, List<String>>{

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> subLocalityNames = new ArrayList<>();
            try {
                String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
                String queryUrl = "http://apis.data.go.kr/B551011/KorPetTourService/areaCode?serviceKey="
                        + code
                        + "&areaCode=" + selectedAreaCode
                        + "&pageNo=1&numOfRows=50&MobileOS=ETC&MobileApp=AppTest";

                Log.d("queryUrl   ", "queryUrl   " + queryUrl);

                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                int eventType = parser.getEventType();
                String tagName;
                String subLocalityName = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("name")) {
                            parser.next();
                            subLocalityName = parser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("item") && subLocalityName != null) {
                            subLocalityNames.add(subLocalityName);
                            subLocalityName = null;
                        }
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return subLocalityNames;
        }

        @Override
        protected void onPostExecute(List<String> subLocalityNames) {
            super.onPostExecute(subLocalityNames);
            if (!subLocalityNames.isEmpty()) {
                setupSubLocalitySpinner(subLocalityNames);
            }
        }
    }

    private void setupSubLocalitySpinner(List<String> subLocalityNames) {
        Spinner subLocalitySpinner = findViewById(R.id.subLocality);
        subLocalityNames.add(0, "시/군/구 선택");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_list,
                subLocalityNames
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(getColor(R.color.gray));
                } else {
                    tv.setTextColor(getColor(R.color.black));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.item_list);
        subLocalitySpinner.setAdapter(adapter);
        subLocalitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.d("Spinner", "선택된 아이템: " + selectedItem);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}

