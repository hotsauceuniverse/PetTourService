package com.seyoung.pettourservice;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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
    private String selectedSubLocalityCode; // 선택된 SubLocalityCode를 저장할 변수
    private String selectedTourTypeCode;    // 선택된 TourTypeCode를 저장할 변수
    Button searchBtn;
    RecyclerView tourResultRecycle;
    private TourTypeAdapter mTourTypeAdapter;
    String selectedTourItem;
    LottieAnimationView noDataAnim;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new getAreaFromLocation().execute();

        // 하위 지역 스피너 초기화 (더미 데이터 설정)
        Map<String, String> dummySubLocality = new LinkedHashMap<>();
        setupSubLocalitySpinner(dummySubLocality);

        new getTourType().execute();

        searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new getSearchTour().execute();
            }
        });
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
                String name = null;
                String codeValue = null;

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
                Log.d("selectedAreaCode   ", "Selected Area Code   " + selectedAreaCode);

                new getsubLocalityFromLocation().execute(); // 하위 지역 데이터 로드
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public class getsubLocalityFromLocation extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            Map<String, String> subLocalityNames = new LinkedHashMap<>();
            try {
                String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
                String queryUrl = "http://apis.data.go.kr/B551011/KorPetTourService/areaCode?serviceKey="
                        + code
                        + "&areaCode=" + selectedAreaCode
                        + "&pageNo=1&numOfRows=50&MobileOS=ETC&MobileApp=AppTest";

                Log.d("queryUrl   ", "queryUrl   " + queryUrl);
                Log.d("selectedAreaCode   ", "selectedAreaCode   " + selectedAreaCode);

                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                int eventType = parser.getEventType();
                String tagName;
                String subLocalityName = null;
                String codeValue = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("name")) {
                            parser.next();
                            subLocalityName = parser.getText();
                        } else if (tagName.equals("code")) {
                            parser.next();
                            codeValue = parser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("item") && subLocalityName != null && codeValue != null) {
                            subLocalityNames.put(subLocalityName, codeValue);
                            subLocalityName = null;
                            codeValue = null;
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
        protected void onPostExecute(Map<String, String> subLocalityNames) {
            super.onPostExecute(subLocalityNames);
            if (!subLocalityNames.isEmpty()) {
                setupSubLocalitySpinner(subLocalityNames);
            }
        }
    }

    private void setupSubLocalitySpinner(Map<String, String> subLocalityNames) {
        Spinner subLocalitySpinner = findViewById(R.id.subLocality);
        List<String> subLocalityList = new ArrayList<>(subLocalityNames.keySet());
        subLocalityList.add(0, "시/군/구 선택");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_list,
                subLocalityList
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
                Log.d("Spinner1   ", "선택된 아이템   " + selectedItem);

                String selectedSubName = subLocalityList.get(position);
                selectedSubLocalityCode = subLocalityNames.get(selectedSubName);
                Log.d("selectedSubLocalityCode   ", "selectedSubLocalityCode   " + selectedSubLocalityCode);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public class getTourType extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            Map<String, String> tourType = new LinkedHashMap<>();

            try {
                String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
                String queryUrl = "http://apis.data.go.kr/B551011/KorPetTourService/categoryCode?serviceKey="
                        + code
                        + "&numOfRows=10&pageNo=1&MobileOS=ETC&MobileApp=AppTest";

                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                int eventType = parser.getEventType();
                String tagName;
                String typeName = null;
                String codeValue = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("name")) {
                            parser.next();
                            typeName = parser.getText();
                        } else if (tagName.equals("code")) {
                            parser.next();
                            codeValue = parser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("item") && typeName != null && codeValue != null) {
                            tourType.put(typeName, codeValue);
                            typeName = null;
                        }
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return tourType;
        }

        @Override
        protected void onPostExecute(Map<String, String> tourTypes) {
            super.onPostExecute(tourTypes);
            if (!tourTypes.isEmpty()) {
                setupTourTypeSpinner(tourTypes);
            }
        }
    }

    private void setupTourTypeSpinner(Map<String, String> tourTypes) {
        Spinner TourType = findViewById(R.id.tour_type);
        List<String> tourTypesList = new ArrayList<>(tourTypes.keySet());
        tourTypesList.add(0, "관광 타입 선택");

        ArrayAdapter<String> tourAdapter = new ArrayAdapter<String>(
                this,
                R.layout.item_list,
                tourTypesList
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
        tourAdapter.setDropDownViewResource(R.layout.item_list);
        TourType.setAdapter(tourAdapter);
        TourType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
//                String selectedItem = (String) parent.getItemAtPosition(position);
                selectedTourItem = (String) parent.getItemAtPosition(position);
                Log.d("Spinner2   ", "선택된 아이템   " + selectedTourItem);

                String selectedTourType = tourTypesList.get(position);
                selectedTourTypeCode = tourTypes.get(selectedTourType);
                Log.d("selectedTourTypeCode   ", "selectedTourTypeCode   " + selectedTourTypeCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public class getSearchTour extends AsyncTask<Void, Void, Map<String, Map<String, String>>> {

        @Override
        protected Map<String, Map<String, String>> doInBackground(Void... voids) {
            Map<String, Map<String, String>> searchTour = new LinkedHashMap<>();
            try {
                String code = "e8KTlQRE/BEp0/kRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw/Q2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ==";
                String queryUrl = "http://apis.data.go.kr/B551011/KorPetTourService/areaBasedList?serviceKey="
                        + code
                        + "&pageNo=1&numOfRows=50&listYN=Y&arrange=A"
                        + "&areaCode=" + selectedAreaCode
                        + "&sigunguCode=" + selectedSubLocalityCode
                        + "&cat1=" + selectedTourTypeCode
                        + "&MobileOS=ETC&MobileApp=AppTest";

                URL url = new URL(queryUrl);
                InputStream inputStream = url.openStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(inputStream, "UTF-8"));

                int eventType = parser.getEventType();
                String tagName;
                String title = null;
                String addr = null;
                String type = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("title")) {
                            parser.next();
                            title = parser.getText();
                        } else if (tagName.equals("addr1")) {
                            parser.next();
                            addr = parser.getText();
                        } else if (tagName.equals("cat1")) {
                            parser.next();
                            type = parser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        if (tagName.equals("item") && title != null && addr != null && type != null) {
                            // 개별 데이터를 Map에 저장
                            Map<String, String> itemData = new LinkedHashMap<>();
                            itemData.put("addr", addr);
                            itemData.put("type", type);

                            // title을 키로 전체 데이터를 저장
                            searchTour.put(title, itemData);

                            // 변수 초기화
                            title = null;
                            addr = null;
                            type = null;
                        }
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return searchTour;
        }

        @Override
        protected void onPostExecute(Map<String, Map<String, String>> tourMapMap) {
            super.onPostExecute(tourMapMap);
            if (!tourMapMap.isEmpty()) {
                setupTourList(tourMapMap);
                noDataAnim.setVisibility(View.GONE);
            } else {
                // RecyclerView에 "데이터가 없습니다" 메시지 표시
                tourResultRecycle = findViewById(R.id.tour_result_recycle);
                tourResultRecycle.setLayoutManager(new LinearLayoutManager(TourType.this));
                List<TourTypeData> emptyList = new ArrayList<>();
                TourTypeAdapter emptyAdapter = new TourTypeAdapter(emptyList);
                tourResultRecycle.setAdapter(emptyAdapter);

                noDataAnim = findViewById(R.id.no_data_anim);
                noDataAnim.setVisibility(View.VISIBLE);
            }
        }
    }

//    private void setupTourList(Map<String, Map<String, String>> tourMapMap) {
//        tourResultRecycle = findViewById(R.id.tour_result_recycle);
//        mTourTypeAdapter = new TourTypeAdapter((List<TourTypeData>) tourMapMap);
//        tourResultRecycle.setAdapter(mTourTypeAdapter);
//        tourResultRecycle.setHasFixedSize(true);
//        tourResultRecycle.setLayoutManager(new LinearLayoutManager(this));
//    }

    private void setupTourList(Map<String, Map<String, String>> tourMapMap) {
        tourResultRecycle = findViewById(R.id.tour_result_recycle);
        // java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to java.util.List 에러
        // tourMapMap이 Map<String, Map<String, String>> 타입인데 이를 List<TourTypeData>로 강제 변환하려고 했기 때문에 발생
        // Map 데이터를 List<TourTypeData>로 변환
        List<TourTypeData> tourTypeDataList = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : tourMapMap.entrySet()) {
            String title = entry.getKey();
            Map<String, String> itemData = entry.getValue();
            String address = itemData.get("addr");
            String tourType = selectedTourItem;
            Log.d("tourType   ", "tourType   " + tourType);

            // TourTypeData 객체 생성
            TourTypeData tourTypeData = new TourTypeData(title, tourType, address);
            tourTypeDataList.add(tourTypeData);
        }
        // 어댑터에 리스트 전달
        mTourTypeAdapter = new TourTypeAdapter(tourTypeDataList);
        tourResultRecycle.setAdapter(mTourTypeAdapter);
        tourResultRecycle.setHasFixedSize(true);
        tourResultRecycle.setLayoutManager(new LinearLayoutManager(this));

    }
}
