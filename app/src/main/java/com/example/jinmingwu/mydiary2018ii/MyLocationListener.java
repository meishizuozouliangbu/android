package com.example.jinmingwu.mydiary2018ii;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

/**
 * Created by jinmingwu on 2018/6/21.
 */

class MyLocationListener extends BDAbstractLocationListener {
    double latitude = 0;    //纬度信息
    double longitude = 0;   //经度信息
    float radius = 0;    //获取定位精度，默认值为0.0f

    String coorType = null;
    //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

    int errorCode = 0;
    //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

    String addr = null;    //获取详细地址信息
    String country = null;    //获取国家
    String province = null;    //获取省份
    String city = null;    //获取城市
    String district = null;    //获取区县
    String street = null;    //获取街道信息

    @Override


    public void onReceiveLocation(BDLocation location) {
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

        latitude = location.getLatitude();    //获取纬度信息
        longitude = location.getLongitude();    //获取经度信息
        radius = location.getRadius();    //获取定位精度，默认值为0.0f

        coorType = location.getCoorType();
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

        errorCode = location.getLocType();
        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

        addr = location.getAddrStr();    //获取详细地址信息
        country = location.getCountry();    //获取国家
        province = location.getProvince();    //获取省份
        city = location.getCity();    //获取城市
        district = location.getDistrict();    //获取区县
        street = location.getStreet();    //获取街道信息
    }

    public String getCountry(){
        return this.country;
    }

    public String getProvince(){
        return this.province;
    }

    public String getCity(){
        return this.city;
    }

    public String getDistrict(){
        return this.district;
    }

    public String getStreet(){
        return this.street;
    }

    public String getAddr(){
        return this.addr;
    }

    public void stop(){

    }
}
