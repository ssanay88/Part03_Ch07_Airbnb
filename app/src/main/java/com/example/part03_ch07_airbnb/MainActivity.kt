package com.example.part03_ch07_airbnb

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.part03_ch07_airbnb.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

// 클래스의 상속과 인터페이스의 구현은 : 으로 표기
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)    // mapView 생명주기 연결

        mapView.getMapAsync(this)    // getMapAsync : 람다로 naverMap을 불러와서 정의하는 방법과 콜백메서드를 통해 실행 , 여기서는 콜백으로 진행



    }

    // OnMapReadyCallback 인터페이스 안에있는 함수 구현
    override fun onMapReady(map: NaverMap) {

        naverMap = map

        naverMap.maxZoom = 18.0    // 최대 줌
        naverMap.minZoom = 10.0    // 최소 줌

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(35.138927, 129.105814))
        naverMap.moveCamera(cameraUpdate)    // 처음 시작 카메라 위치를 업데이트

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true   // 현 위치 버튼 활성화 , 권한을 받아와야 실행 가능

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        val marker = Marker()    // 마커 객체 생성
        marker.position = LatLng(35.138299, 129.103646)    // 마커 위치
        marker.map = naverMap    // 지도 지정
        marker.icon = MarkerIcons.BLACK
        marker.iconTintColor = Color.RED

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        // 구글에서 지원하는 라이브러리를 통해 권한 팝업을 쉽게 호출
        // 위치 권한이 허용되면 현재 위치 트래킹 가능
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None    // 권한이 거부됨을 네이버맵에 알려줌
            }
            return
        }




    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


}


/*
Naver Map API 사용하기
ViewPager2 사용하기
FrameLayout 알아보기
CoordinatorLayout 사용하기 = FrameLayout + 기능
BottomSheetBehavior 사용하기
Retrofit 사용하기
Glide 사용하기

에어비엔비
Naver Map API를 이용해서 지도를 띄우고 활용할 수 있음
Mock API에서 예약가능 숙소 목록을 받아와서 지도에 표시할 수 있음
BottomSheetView를 활용해서 예약 가능 숙소 목록을 인터렉션하게 표시할 수 있음
ViewPager2를 활용해서 현재 보고있는 숙소를 표시할 수 있음
숙소버튼을 눌러 현재 보고 있는 숙소를 앱 외부로 공유할 수 있음
 */