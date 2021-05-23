package com.example.lizhehuigis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapLayer;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.weather.LanguageType;
import com.baidu.mapapi.search.weather.OnGetWeatherResultListener;
import com.baidu.mapapi.search.weather.WeatherDataType;
import com.baidu.mapapi.search.weather.WeatherResult;
import com.baidu.mapapi.search.weather.WeatherSearch;
import com.baidu.mapapi.search.weather.WeatherSearchOption;
import com.baidu.mapapi.search.weather.WeatherSearchRealTime;
import com.baidu.mapapi.search.weather.WeatherServerType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.COMPASS;
import static com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING;
import static com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";//权限参数
    private MapView mMapView = null;
    private BaiduMap baiduMap=null;//地图控制器
    private RadioButton pt,weix;//普通地图，卫星地图按钮
    private Button limit;//限速显示按钮
    private ImageView limitimageView;//限速图片
    private boolean count=false;//限速按钮状态变量
    private int limitValue;//限速速度
    private boolean isAutoClick=false;//自动点击状态变量
    private boolean isreturncenten=false;//普通地图模式返回当前位置状态参数
    private CheckBox lukuang,reli;//路况，热力按钮
    private RadioGroup group;
    private final LatLng centerlatlatlng=new LatLng(30.519115,114.350384);//中心点位置
    private LocationClient mLocationClient;//定位参数
    boolean isFirstLocation=true;//首次定位状态变量
    private LatLng locationPoint =null;//当前位置存储变量
    private MyLocationConfiguration.LocationMode locationMode=null;//定位模式参数

    private WeatherSearch mWeatherSearch;//创建天气检索参数
    // 自定义折线状态存储
    private Polyline prePolyline=null,mPolyline0,mPolyline1,mPolyline2,mPolyline3,mPolyline4,mPolyline5,mPolyline6,mPolyline7,
            mPolyline8,mPolyline9,mPolyline10,mPolyline11,mPolyline12;
            ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermissionMethod();//权限请求
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        getview();
        autoClickPos(MainActivity.this, 520,1380);//设置自动点击
        baiduMap=mMapView.getMap();
        addpolyline();

        //图片显示按钮监听器
        limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==limit){//判断路的限速信息图片
                    try {
                        limitValue=prePolyline.getExtraInfo().getInt("limitvalue");//此时prePolyline=当前polyline
                    }catch(Exception a){
                        Toast.makeText(MainActivity.this, "请选择道路", Toast.LENGTH_SHORT).show();
                        count=!count;
                    }
                    switch (limitValue){
                        case 10:
                            limitimageView.setImageResource(R.drawable.icon_limitspeed10);break;
                        case 20:
                            limitimageView.setImageResource(R.drawable.icon_limitspeed20);break;
                        case 30:
                            limitimageView.setImageResource(R.drawable.icon_limitspeed30);break;
                        case 40:
                            limitimageView.setImageResource(R.drawable.icon_limitspeed40);break;
                        case 50:
                            limitimageView.setImageResource(R.drawable.icon_limitspeed50);break;
                        case 60:
                            limitimageView.setImageResource(R.drawable.icon_limitspeed60);break;
                    }

                    if(!count)//不可见设置为可见
                    { limitimageView.setVisibility(View.VISIBLE);
                    count=true;
                    isAutoClick=false;}
                    else {//可见设置为不可见
                        limitimageView.setVisibility(View.INVISIBLE);
                        count=false;
                        isAutoClick=true;
                    }
                }
            }
        });
        //路况显示按钮监听器
        lukuang.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    //开启交通图
                    baiduMap.setTrafficEnabled(lukuang.isChecked());
                }

        });
        //热力显示按钮监听器
        reli.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //变更3D
                myoverlook(-45);
            }
        });
        //地图类型显示按钮监听器
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {//普通地图或者卫星图，声明+方法写在一起
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(pt.isChecked())
                {
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    myoverlook(0);
                }
                if(weix.isChecked())
                {
                    baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
            }
        });
        baiduMap.setMyLocationEnabled(true);//开启地图的定位图层
        //polyline点击事件监听器
        baiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                setPrePolylineFalse();//之前点击的polyline隐藏
                setNowpolylineTrue(polyline);//设置当前line可见
                if (polyline == mPolyline0) {
                    Toast.makeText(MainActivity.this, "Click on polyline0", Toast.LENGTH_SHORT).show();
                } else if (polyline ==  mPolyline1) {
                    Toast.makeText(MainActivity.this, "Click on polyline1", Toast.LENGTH_SHORT).show();
                } else if (polyline ==  mPolyline2) {
                    Toast.makeText(MainActivity.this, "Click on polyline2", Toast.LENGTH_SHORT).show();
                } else if (polyline ==  mPolyline3) {
                    Toast.makeText(MainActivity.this, "Click on polyline3", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline4) {
                    Toast.makeText(MainActivity.this, "Click on polyline4", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline5) {
                    Toast.makeText(MainActivity.this, "Click on polyline5", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline6) {
                    Toast.makeText(MainActivity.this, "Click on polyline6", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline7) {
                    Toast.makeText(MainActivity.this, "Click on polyline7", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline8) {
                    Toast.makeText(MainActivity.this, "Click on polyline8", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline9) {
                    Toast.makeText(MainActivity.this, "Click on polyline9", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline10) {
                    Toast.makeText(MainActivity.this, "Click on polyline10", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline11) {
                    Toast.makeText(MainActivity.this, "工大路", Toast.LENGTH_SHORT).show();
                }else if (polyline ==  mPolyline12) {
                    Toast.makeText(MainActivity.this, "Click on polyline12", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
    //权限开启
    private void getPermissionMethod() {
        List<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        Log.i(TAG, "getPermissionMethod: permissionListSize:"+permissionList.size());
        if (!permissionList.isEmpty()){ //权限列表不是空
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            Log.i(TAG, "getPermissionMethod: requestLocation !permissionList.isEmpty()里");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for (int result:grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须统一所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }else
                {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    public  void setNowpolylineTrue(Polyline nowpolyline){//当前点击的polyline显示
        nowpolyline.setVisible(true);
        prePolyline=nowpolyline;
    }
    public void setPrePolylineFalse(){//之前点击的polyline隐藏
         if(prePolyline==mPolyline0) mPolyline0.setVisible(false);
        else if(prePolyline==mPolyline1) mPolyline1.setVisible(false);
        else if(prePolyline==mPolyline2) mPolyline2.setVisible(false);
        else if(prePolyline==mPolyline3) mPolyline3.setVisible(false);
        else if(prePolyline==mPolyline4) mPolyline4.setVisible(false);
        else if(prePolyline==mPolyline5) mPolyline5.setVisible(false);
        else if(prePolyline==mPolyline6) mPolyline6.setVisible(false);
        else if(prePolyline==mPolyline7) mPolyline7.setVisible(false);
        else if(prePolyline==mPolyline8) mPolyline8.setVisible(false);
        else if(prePolyline==mPolyline9) mPolyline9.setVisible(false);
        else if(prePolyline==mPolyline10) mPolyline10.setVisible(false);
        else if(prePolyline==mPolyline11) mPolyline11.setVisible(false);
        else if(prePolyline==mPolyline12) mPolyline12.setVisible(false);
    }
    /*public void setAllprePolylinetrue()//检测用
    {
         mPolyline0.setVisible(true);
         mPolyline1.setVisible(true);
         mPolyline2.setVisible(true);
         mPolyline3.setVisible(true);
         mPolyline4.setVisible(true);
         mPolyline5.setVisible(true);
         mPolyline6.setVisible(true);
         mPolyline7.setVisible(true);
         mPolyline8.setVisible(true);
         mPolyline9.setVisible(true);
         mPolyline10.setVisible(true);
         mPolyline11.setVisible(true);
         mPolyline12.setVisible(true);
    }*/
    //更改地图中心
    public void setCenter(LatLng i)
    {
        //定义中心坐标
        MapStatus mapStatus=new MapStatus.Builder().target(i).zoom(19).build();
        MapStatusUpdate mapStatusUpdate= MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }
    //添加图片覆盖物30.521745,114.352218
    public void  addoverlay(double x,double y)
    {
//构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_light);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(new LatLng(x,y))
                .perspective(false)
                .icon(bitmap);
//在地图上添加Marker，并显示
        baiduMap.addOverlay(option);
    }
    //将java代码和xml控件联系起来
    public void getview()
    {
        pt=findViewById(R.id.pt_radioButton);
        weix=findViewById(R.id.weix_radioButton);
        lukuang=findViewById(R.id.lukuang_checkBox);
        reli=findViewById(R.id.reli_checkBox);
        group=findViewById(R.id.group);
        limit=findViewById(R.id.limit_Button);
        limitimageView=findViewById(R.id.photo);
        mMapView = findViewById(R.id.bmapView);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mLocationClient.stop();//关闭定位
        baiduMap.setMyLocationEnabled(false);//关闭定位
    }

    //添加选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,1,1,"查看交通灯");
        menu.add(Menu.NONE,2,2,"普通定位");
        menu.add(Menu.NONE,3,3,"跟随定位");
       // menu.add(Menu.NONE,4,4,"罗盘定位");
       // menu.add(Menu.NONE,5,5,"当前位置");
        menu.add(Menu.NONE,6,6,"查询天气");
        return  true;
    }
//选项菜单执行
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

       switch (item.getItemId())
       {
           case 1://更改地图中心
               setCenter(centerlatlatlng);//设置中心
               addoverlay(30.521745,114.352218); //添加覆盖物交通灯1
               addoverlay(30.524386,114.352707);//添加覆盖物交通灯2
               isAutoClick=false;//设置自动点击事件状态变量=关闭
               break;
           case 2://普通模式定位
               myoverlook(0);//设置俯角为0
               locationMode=NORMAL; //普通模式
               if(!isreturncenten)
               {
                   isreturncenten=true;
                   //定位初始化
                   mylocationoption();
               }else setCenter(locationPoint);
               isAutoClick=false;//          设置自动点击事件状态变量=开启
               break;
           case 3://跟随模式
               myoverlook(0);//设置俯角为0
               locationMode=FOLLOWING;
               mylocationoption();
               isAutoClick=true;//          设置自动点击事件状态变量=开启
               break;
           case 4://罗盘模式
               locationMode=COMPASS;
               mylocationoption();
               isAutoClick=true;//          设置自动点击事件状态变量=开启
               break;
           case 5://当前位置
               setCenter(locationPoint);
               isAutoClick=false;//设置自动点击事件状态变量=关闭
               break;
           case 6://
               setCenter(centerlatlatlng);
               weathersearch("420111");//珞南街道
               isAutoClick=false;//设置自动点击事件状态变量=关闭
               break;
       }
       return true;
    }

    //设置天气情况
    public void  weathersearch(String id){
        mWeatherSearch = WeatherSearch.newInstance();//创建天气检索实例
        WeatherSearchOption weatherSearchOption = new WeatherSearchOption()
                .weatherDataType(WeatherDataType.WEATHER_DATA_TYPE_ALL)
                .districtID(id);
        mWeatherSearch.setWeatherSearchResultListener(new OnGetWeatherResultListener() {
            @Override
            public void onGetWeatherResultListener(final WeatherResult weatherResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        popupWeatherDialog(weatherResult);
                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption);
    }
    private void popupWeatherDialog(WeatherResult weatherResult){
        if (null == weatherResult) {
            return;
        }
        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
        if (null == weatherSearchRealTime) {
            return;
        }
        final AlertDialog.Builder weatherDialog =
                new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.weather, null);
        if (null == view) {
            return;
        }

        TextView txtTemp = view.findViewById(R.id.txtTemp);
        String temp = "温度：" + weatherSearchRealTime.getTemperature() + "℃";
        txtTemp.setText(temp);

        TextView txtPhenomenon = view.findViewById(R.id.txtPhenomenon);
        String phenomenon = "天气现象："+weatherSearchRealTime.getPhenomenon();
        txtPhenomenon.setText(phenomenon);

        TextView txtRelativeHumdidity = view.findViewById(R.id.txtRelativeHumidity);
        String relativeHumidity =
                "相对湿度：" + weatherSearchRealTime.getRelativeHumidity() + "%";
        txtRelativeHumdidity.setText(relativeHumidity);

        TextView txtSensoryTemp = view.findViewById(R.id.txtSensoryTemp);
        String sensoryTemp = "体感温度：" + String.valueOf(weatherSearchRealTime.getSensoryTemp()) + "℃";
        txtSensoryTemp.setText(sensoryTemp);

        TextView txtUpdateTime = view.findViewById(R.id.txtUpdateTime);
        String s=weatherSearchRealTime.getUpdateTime();
            String year = s.substring(0,4);
            String month = s.substring(4,6);
            String day = s.substring(6,8);
            String hour = s.substring(8,10);
            String minute = s.substring(10,12);
            String second = s.substring(12,14);
        String updateTime = "更新时间：" +year+"."+month+"."+day+" "+hour+":"+minute+":"+second ;
        txtUpdateTime.setText(updateTime);

        weatherDialog.setTitle("实时天气").setView(view).create();
        weatherDialog.show();
    }
    //设置俯角为0
    public void myoverlook(int i){//设置俯角为0
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(i);
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
    }
    //设置定位信息
    public void mylocationoption(){
        mLocationClient = new LocationClient(this);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }
    //设置定位监听器信息，内部类，分开了，应该也可以声明和方法放一起
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
            locationPoint =new LatLng(location.getLatitude(),location.getLongitude());//当前位置信息
            //状态改变,地图SDK支持三种定位模式：NORMAL（普通态）, FOLLOWING（跟随态）, COMPASS（罗盘态）
            MyLocationConfiguration myLocationConfiguration=new MyLocationConfiguration(locationMode,true,null );
            baiduMap.setMyLocationConfiguration(myLocationConfiguration);


            if(isFirstLocation)  //首次定位 更新地图状态
            {
                isFirstLocation=false;
                LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
                MapStatusUpdate mapStatusUpdate=MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.setMapStatus(mapStatusUpdate);

                baiduMap.setLayerClickable(MapLayer.MAP_LAYER_LOCATION,false );
                baiduMap.switchLayerOrder(MapLayer.MAP_LAYER_LOCATION, MapLayer.MAP_LAYER_OVERLAY);

                mapStatusUpdate=MapStatusUpdateFactory.zoomTo(19);
                baiduMap.animateMapStatus(mapStatusUpdate);
            }

        }
    }

    //绘制mPolyline折线对象
    public void addpolyline(){
        mPolyline0=mPolyline(new LatLng(30.518507,114.349848),new LatLng(30.519487,114.350028),10);
        mPolyline1=mPolyline(new LatLng(30.519487,114.350028),new LatLng(30.519316,114.350899),10);
        mPolyline2=mPolyline(new LatLng(30.519316,114.350899),new LatLng(30.518861,114.350742),new LatLng(30.518426, 114.350643),20);
        mPolyline3=mPolyline(new LatLng(30.518426,114.350643),new LatLng(30.518496,114.349843),20);
        mPolyline4=mPolyline(new LatLng(30.519934,114.348563),new LatLng(30.519487,114.35001),20);
        mPolyline5=mPolyline(new LatLng(30.520596,114.350099),new LatLng(30.520463,114.350261),new LatLng(30.520405,114.350616),20);
        mPolyline6=mPolyline(new LatLng(30.521502,114.351559),new LatLng(30.520599,114.351451),new LatLng(30.520382,114.351258),new LatLng(30.520335,114.350921),20);
        mPolyline7=mPolyline(new LatLng(30.519616,114.349938),new LatLng(30.520471,114.350261),10);
        mPolyline8=mPolyline(new LatLng(30.520405,114.350611),new LatLng(30.521941,114.351007),10);
        mPolyline9=mPolyline(new LatLng(30.518706,114.348491),new LatLng(30.518643,114.348918),new LatLng(30.518503,114.349848),20);
        mPolyline10=mPolyline(new LatLng(30.518414,114.350652),new LatLng(30.518324,114.351335),new LatLng(30.51827,114.351487),20);
        mPolyline11=mPolyline(new LatLng(30.517119,114.351339),new LatLng(30.520292,114.35195),new LatLng(30.524403,114.352718),30);
        mPolyline12=mPolyline(new LatLng(30.520685,114.350054),new LatLng(30.52205,114.35036),10);

    }
    //polyline函数2点
    public Polyline mPolyline(LatLng start, LatLng end,int LimitSpeed){
        //构建折线点坐标
        Polyline myname;
        List<LatLng> line = new ArrayList<LatLng>();
        line.add(start);
        line.add(end);
        //设置折线的属性
        Bundle bundle = new Bundle();
        bundle.putInt("limitvalue", LimitSpeed);
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(line)
                .visible(false)
                .extraInfo(bundle);
        //在地图上绘制折线
        myname = (Polyline) baiduMap.addOverlay(mOverlayOptions);
        return myname;
    }
    //polyline函数3点  重载
    public Polyline mPolyline(LatLng start,LatLng node,LatLng end,int LimitSpeed){
        //构建折线点坐标
        Polyline myname;
        List<LatLng> line = new ArrayList<LatLng>();
        line.add(start);
        line.add(node);
        line.add(end);
        //设置折线的属性
        Bundle bundle = new Bundle();
        bundle.putInt("limitvalue", LimitSpeed);
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(line)
                .visible(false)
                .extraInfo(bundle);
        //在地图上绘制折线
        myname = (Polyline) baiduMap.addOverlay(mOverlayOptions);
        return myname;
    }
    //polyline函数4点  重载
    public Polyline mPolyline(LatLng start,LatLng node1,LatLng node2,LatLng end,int LimitSpeed){
        //构建折线点坐标
        Polyline myname;
        List<LatLng> line = new ArrayList<LatLng>();
        line.add(start);
        line.add(node1);
        line.add(node2);
        line.add(end);
        //设置折线的属性

        Bundle bundle = new Bundle();
        bundle.putInt("limitvalue", LimitSpeed);

        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(line)
                .visible(false)
                .extraInfo(bundle);
        //在地图上绘制折线
        myname = (Polyline) baiduMap.addOverlay(mOverlayOptions);
        return myname;
    }


    /**
     * 传入在屏幕中的坐标，坐标左上角为基准
     *
     * @param act 传入Activity对象
     * @param x   需要点击的x坐标
     * @param y   需要点击的x坐标
     */
    public void autoClickPos(Activity act, final double x, final double y)//自动点击方法，和主线程同步进行，故用run()方法
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(isAutoClick)//启动线程和中断线程
                    {
                        // 线程睡眠0.1s
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 利用ProcessBuilder执行shell命令
                        String[] order = {"input", "tap", "" + x, "" + y};
                        try {
                            new ProcessBuilder(order).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(5000);//5秒点一次
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else Thread.interrupted();
                }
            }
        }).start();

    }
}