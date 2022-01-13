package com.example.part03_ch07_airbnb

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.part03_ch07_airbnb.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 클래스의 상속과 인터페이스의 구현은 : 으로 표기
class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var currentLocationBtn: LocationButtonView

    // 리사이클러뷰 클릭리스너 생성
    private val viewPagerAdapter = HouseViewPagerAdapter(itemClicked = {
        // 뷰페이저 아이템 클릭 시 공유하는 인텐트
        val intent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "[지금 이 가격에 예약하세요!!] ${it.title} ${it.price} 사진보기 : ${it.imgUrl}")
                type = "text/plain"
            }

        startActivity(Intent.createChooser(intent, null))

    })

    private val recyclerViewAdapter = HouseListAdapter()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)    // mapView 생명주기 연결

        mapView.getMapAsync(this)    // getMapAsync : 람다로 naverMap을 불러와서 정의하는 방법과 콜백메서드를 통해 실행 , 여기서는 콜백으로 진행

        viewPager = binding.houseViewPager
        viewPager.adapter = viewPagerAdapter    // 뷰페이저 어댑터 설정

        recyclerView = binding.bottomSheetLayout.recyclerView
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 뷰페이저에서 집을 선택할 경우 해당 집의 마커로 이동
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            // 페이지가 선택된 경우 콜백
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]    // 선택된 position에 해당하는 HouseModel을 가져온다.

                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat,selectedHouseModel.lng))    // 선택된 집의 좌표로 카메라 업데이트 선언
                    .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)    // 카메라 이동


            }
        })

    }

    // OnMapReadyCallback 인터페이스 안에있는 함수 구현
    override fun onMapReady(map: NaverMap) {

        naverMap = map

        naverMap.maxZoom = 18.0    // 최대 줌
        naverMap.minZoom = 10.0    // 최소 줌

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(35.138927, 129.105814))
        naverMap.moveCamera(cameraUpdate)    // 처음 시작 카메라 위치를 업데이트

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false   // 현 위치 버튼 활성화 , 권한을 받아와야 실행 가능

        currentLocationBtn = binding.currentLocationBtn
        currentLocationBtn.map = naverMap

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        // API를 통해 위치를 불러올 때 순서때문에 여기서 실행
        // 우선 MapView를 띄운 후 API를 통해 위치 리스트를 받아와서 마커로 표시해야 한다.
        getHouseListFromApi()



    }


    private fun getHouseListFromApi() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                .enqueue(object: Callback<HouseDto>{
                    // 실행 성공
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if (response.isSuccessful.not()) {
                            // 실패 처리에 대한 구현
                            return
                        }

                        response.body()?.let { dto ->
                            Log.d("Retrofit",dto.toString())
                            updateMarker(dto.items)
                            viewPagerAdapter.submitList(dto.items)    // 뷰페이저 리스트에 아이템 추가
                            recyclerViewAdapter.submitList(dto.items)    // bottomSheet에 보여주는 리사이클러뷰 리스트에 아이템 추가

                            binding.bottomSheetLayout.bottomSheetTitleTextView.text = "${dto.items.size}개의 숙소"

                        }
                    }

                    // 실패 처리에 대한 구현
                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                        Log.e("Retrofit",t.toString())
                    }

                })
        }

    }


    private fun updateMarker(houses: List<HouseModel>) {
        houses.forEach { house ->

            val marker = Marker()
            marker.position = LatLng(house.lat,house.lng)
            // TODO 마커 클릭 리스너
            marker.onClickListener = this

            marker.map = naverMap
            marker.tag = house.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED

        }
    }

    // 마커를 클릭했을 경우
    override fun onClick(overlay: Overlay): Boolean {
        // 뷰 페이저에 들어있는 리스트에서 제일 위에 있는 값을 불러오는 명령, 없으면 Null 반환
        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag    // 선택된 마커의 태그의 일치하는 id를 가지는 모델로 선택
        }

        // 선택된 모델이 null이 아닐 경우
        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)    // 리스트에서 해당 모델의 인덱스를 position으로 지정
            viewPager.currentItem = position
        }

        return true

    }



    // 권한 요청에 대한 결과 처리 함수
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