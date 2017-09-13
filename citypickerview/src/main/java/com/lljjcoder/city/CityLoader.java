package com.lljjcoder.city;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lljjcoder.city.bean.CityBean;
import com.lljjcoder.city.bean.DistrictBean;
import com.lljjcoder.city.bean.ProvinceBean;

/**
 * 省市区数据加载缓存工具
 * Created by 01370737 on 2017/9/13.
 */

class CityLoader {

    private static final String CITY_DATE_FILE_NAME = "city_20170724.json";
    private static final String LOG_TAG = "CityLoader";
    @SuppressLint("StaticFieldLeak") //we static reference application context, no memoryleak
    private static volatile CityLoader sCityLoader;
    private final Context mContext;
    //省份数据
    private ArrayList<ProvinceBean> mProvinceBeanArrayList;

    //城市数据
    ArrayList<ArrayList<CityBean>> mCityBeanArrayList;

    //地区数据
    ArrayList<ArrayList<ArrayList<DistrictBean>>> mDistrictBeanArrayList;
    private ProvinceBean mProvinceBean;
    private CityBean mCityBean;
    private DistrictBean mDistrictBean;
    private ProvinceBean[] mProvinceBeenArray;
    /**
     * key - 省 value - 市
     */
    private Map<String, CityBean[]> mPro_CityMap = new HashMap<String, CityBean[]>();

    /**
     * key - 市 values - 区
     */
    private Map<String, DistrictBean[]> mCity_DisMap = new HashMap<String, DistrictBean[]>();

    /**
     * key - 区 values - 邮编
     */
    private Map<String, DistrictBean> mDisMap = new HashMap<String, DistrictBean>();

    private CityLoader(Context context) {
        mContext = context.getApplicationContext();
    }

    static CityLoader singleInstance(Context context) {
        if(sCityLoader == null) {
            sCityLoader = new CityLoader(context);
        }
        return sCityLoader;
    }

    synchronized void loadData() {
        if(mProvinceBeanArrayList != null && mProvinceBeanArrayList.size() > 0) {
            Log.v(LOG_TAG, "Data already load province count: " + mProvinceBeanArrayList.size());
            return;
        }
        String cityJson = utils.getJson(mContext, CITY_DATE_FILE_NAME);
        Type type = new TypeToken<ArrayList<ProvinceBean>>() {}.getType();

        mProvinceBeanArrayList = new Gson().fromJson(cityJson, type);
        mCityBeanArrayList = new ArrayList<>(mProvinceBeanArrayList.size());
        mDistrictBeanArrayList = new ArrayList<>(mProvinceBeanArrayList.size());

        //*/ 初始化默认选中的省、市、区，默认选中第一个省份的第一个市区中的第一个区县
        if (mProvinceBeanArrayList != null && !mProvinceBeanArrayList.isEmpty()) {
            mProvinceBean = mProvinceBeanArrayList.get(0);
            List<CityBean> cityList = mProvinceBean.getCityList();
            if (cityList != null && cityList.size() > 0) {
                mCityBean = cityList.get(0);
                List<DistrictBean> districtList = mCityBean.getCityList();
                if (districtList != null && !districtList.isEmpty() && districtList.size() > 0) {
                    mDistrictBean = districtList.get(0);
                }
            }
        }

        //省份数据
        mProvinceBeenArray = new ProvinceBean[mProvinceBeanArrayList.size()];

        for (int p = 0; p < mProvinceBeanArrayList.size(); p++) {

            //遍历每个省份
            ProvinceBean itemProvince = mProvinceBeanArrayList.get(p);

            //每个省份对应下面的市
            ArrayList<CityBean> cityList = itemProvince.getCityList();

            //当前省份下面的所有城市
            CityBean[] cityNames = new CityBean[cityList.size()];

            //遍历当前省份下面城市的所有数据
            for (int j = 0; j < cityList.size(); j++) {
                cityNames[j] = cityList.get(j);

                //当前省份下面每个城市下面再次对应的区或者县
                List<DistrictBean> districtList = cityList.get(j).getCityList();

                ArrayList<DistrictBean> filteredDistricts = new ArrayList<>(districtList.size());
                for (int k = 0; k < districtList.size(); k++) {

                    // 遍历市下面所有区/县的数据
                    DistrictBean districtModel = districtList.get(k);

                    //过滤掉市辖区
                    if(districtModel.getName().equals("市辖区")) {
                        continue;
                    }
                    //存放 省市区-区 数据
                    mDisMap.put(itemProvince.getName() + cityNames[j].getName() + districtList.get(k).getName(),
                            districtModel);

                    filteredDistricts.add(districtModel);

                }
                // 市-区/县的数据，保存到mDistrictDatasMap
                mCity_DisMap.put(itemProvince.getName() + cityNames[j].getName(), //province + city name as key
                        filteredDistricts.toArray(new DistrictBean[filteredDistricts.size()]));

            }

            // 省-市的数据，保存到mCitisDatasMap
            mPro_CityMap.put(itemProvince.getName(), cityNames);

            mCityBeanArrayList.add(cityList);

            ArrayList<ArrayList<DistrictBean>> array2DistrictLists = new ArrayList<>(cityList.size());

            for (int c = 0; c < cityList.size(); c++) {
                CityBean cityBean = cityList.get(c);
                array2DistrictLists.add(cityBean.getCityList());
            }
            mDistrictBeanArrayList.add(array2DistrictLists);

            //赋值所有省份的名称
            mProvinceBeenArray[p] = itemProvince;

        }
    }


    ProvinceBean[] getProvinceArray() {
        return mProvinceBeenArray;
    }

    Map<String, CityBean[]> getProvinceCityMap() {
        return mPro_CityMap;
    }

    Map<String, DistrictBean[]> getCityDistrictMap() {
        return mCity_DisMap;
    }

    Map<String, DistrictBean> getDistrictMap() {
        return mDisMap;
    }
}
