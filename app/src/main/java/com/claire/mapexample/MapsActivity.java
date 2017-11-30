package com.claire.mapexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    //連線至 Google API
    GoogleApiClient mGoogleApiClient;
    FusedLocationProviderClient mFusedLocationClient;
    //要得到位置資訊需使用到位置請求權
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //利用 getFragmentManager()方法取得管理器
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //以非同步的方式取得 GoogleMap 物件
        mapFragment.getMapAsync(this);

        //Google API
        //若找不到LocationServices 就需到gradle加上
        //implementation 'com.google.android.gms:play-services-location:11.6.0'
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest(); //產生位置請求設置物件
        mLocationRequest.setInterval(5000); //設定回報速率，以毫秒為單位
        mLocationRequest.setFastestInterval(2000); //設定最快的回報速率
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //設定優先權
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //啟用地圖上zoom 放大、拉遠控制鈕
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            setMyLocation(); //地圖上會出現我的位置定位按鈕
        }


        // Add a marker in Sydney and move the camera
        LatLng place = new LatLng(25.033408, 121.564099);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 15));
        //第一個方式需要點擊才會出現視窗資訊
//        mMap.addMarker(new MarkerOptions()
//                .position(place)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bubble2)) //更換標記地圖
//                .title("101")
//                .snippet("這是台北101")); //顯示資訊視窗 Info Window


        //第二個方式是不需要點擊
//        Marker marker = mMap.addMarker(new MarkerOptions()
//                .position(place)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bubble2)) //更換標記地圖
//                .title("101")
//                .snippet("這是台北101")); //顯示資訊視窗 Info Window
//        marker.showInfoWindow();


        //第三種方式自訂畫面 info_window.xml layout
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(place)
                .title("101")
                .snippet("這是台北101"));
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.info_window, null);
                TextView title = view.findViewById(R.id.info_title);
                title.setText("Title:" + marker.getTitle());
                TextView snippet = view.findViewById(R.id.info_snippet);
                snippet.setText(marker.getTitle());
                return view;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        //點擊標記事件
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //return false;
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(marker.getTitle())
                        .setMessage(marker.getSnippet())
                        .setPositiveButton("OK", null)
                        .show();
                return false;
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void setMyLocation() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //透過位置服務，取得目前裝置所在
                LocationManager locationManager =
                        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                //設定標準為存取精確
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                //向系統查詢最合適的服務提供者名稱(通常也是"GPS")
                String provider = locationManager.getBestProvider(criteria, true);
                //getLastKnowLocation 取得目前裝置的位子
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    Log.i("LOCATION", "onMyLocationButtonClick: " +
                            location.getLatitude() + "/" + location.getLatitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            15));
                }
                return false;
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0) {
                    //使用者允許權限
                    setMyLocation();
                } else {
                    //使用者拒絕授權，停用MyLocation功能
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //使用GoogleApi取得目前位置
        mFusedLocationClient.getLastLocation().addOnSuccessListener(
                this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null){
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                   //moveMap(latLng);
                   Marker marker = mMap.addMarker(new MarkerOptions()
                           .position(latLng)
                           .title("I am here!!!"));
                   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                   marker.showInfoWindow();
                }
            }
        });

        //使用GoogleApi取得目前位置 FusedLocationApi 已棄用
//        @SuppressLint("MissingPermission")
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (location != null){
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            new LatLng(location.getLatitude(),
//                                    location.getLongitude()), 15));
//                }
    }

    // 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //implements LocationListener
    @Override
    public void onLocationChanged(Location location) {
        //位置有變動時會呼叫
        if (location != null) {
            Log.d("LOCATION", "onLocationChanged: " +
                    "" + location.getLatitude() + "/" + location.getLongitude());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            moveMap(latLng);// 移動地圖
        }

    }
}

