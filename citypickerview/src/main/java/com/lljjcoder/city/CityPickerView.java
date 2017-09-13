package com.lljjcoder.city;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lljjcoder.city.bean.CityBean;
import com.lljjcoder.city.bean.DistrictBean;
import com.lljjcoder.city.bean.ProvinceBean;
import com.lljjcoder.citypickerview.R;
import com.lljjcoder.citypickerview.widget.CanShow;
import com.lljjcoder.citypickerview.widget.wheel.OnWheelChangedListener;
import com.lljjcoder.citypickerview.widget.wheel.WheelView;
import com.lljjcoder.citypickerview.widget.wheel.adapters.ArrayWheelAdapter;

/**
 * 省市区三级选择
 * 作者：liji on 2015/12/17 10:40
 * 邮箱：lijiwork@sina.com
 */
public class CityPickerView implements CanShow, OnWheelChangedListener {

    private Context context;
    private CityLoader dataLoader;
    private PopupWindow popwindow;
    private View popview;
    private WheelView mWheelViewProvince;
    private WheelView mWheelViewCity;
    private WheelView mWheelViewDistrict;
    private RelativeLayout mRelativeTitleBg;
    private TextView mTvOK;
    private TextView mTvTitle;
    private TextView mTvCancel;
    

    private ProvinceBean mProvinceBean;
    private CityBean mCityBean;
    private DistrictBean mDistrictBean;

    //***************************20170822更新************************************//
    
    private OnCityItemClickListener listener;
    
    public interface OnCityItemClickListener {
        void onSelected(ProvinceBean province, CityBean city, DistrictBean district);
        
        void onCancel();
    }
    
    public void setOnCityItemClickListener(OnCityItemClickListener listener) {
        this.listener = listener;
    }
    
    /**
     * Default text color
     */
    public static final int DEFAULT_TEXT_COLOR = 0xFF585858;
    
    /**
     * Default text size
     */
    public static final int DEFAULT_TEXT_SIZE = 18;
    
    // Text settings
    private int textColor = DEFAULT_TEXT_COLOR;
    
    private int textSize = DEFAULT_TEXT_SIZE;
    
    /**
     * 滚轮显示的item个数
     */
    private static final int DEF_VISIBLE_ITEMS = 5;
    
    // Count of visible items
    private int visibleItems = DEF_VISIBLE_ITEMS;
    
    /**
     * 省滚轮是否循环滚动
     */
    private boolean isProvinceCyclic = true;
    
    /**
     * 市滚轮是否循环滚动
     */
    private boolean isCityCyclic = true;
    
    /**
     * 区滚轮是否循环滚动
     */
    private boolean isDistrictCyclic = true;
    
    /**
     * item间距
     */
    private int padding = 5;
    
    /**
     * Color.BLACK
     */
    private String cancelTextColorStr = "#000000";
    
    /**
     * Color.BLUE
     */
    private String confirmTextColorStr = "#0000FF";
    
    /**
     * 标题背景颜色
     */
    private String titleBackgroundColorStr = "#E9E9E9";
    
    /**
     * 标题颜色
     */
    private String titleTextColorStr = "#E9E9E9";
    
    /**
     * 第一次默认的显示省份，一般配合定位，使用
     */
    private String defaultProvinceName = "江苏";
    
    /**
     * 第一次默认得显示城市，一般配合定位，使用
     */
    private String defaultCityName = "常州";
    
    /**
     * 第一次默认得显示，一般配合定位，使用
     */
    private String defaultDistrict = "新北区";
    
    /**
     * 两级联动
     */
    private boolean showProvinceAndCity = false;
    
    /**
     * 标题
     */
    private String mTitle = "选择地区";
    
    /**
     * 设置popwindow的背景
     */
    private int backgroundPop = 0xa0000000;
    
    private CityPickerView(Builder builder) {
        this.textColor = builder.textColor;
        this.textSize = builder.textSize;
        this.visibleItems = builder.visibleItems;
        this.isProvinceCyclic = builder.isProvinceCyclic;
        this.isDistrictCyclic = builder.isDistrictCyclic;
        this.isCityCyclic = builder.isCityCyclic;
        this.context = builder.mContext;
        this.padding = builder.padding;
        this.mTitle = builder.mTitle;
        this.titleBackgroundColorStr = builder.titleBackgroundColorStr;
        this.confirmTextColorStr = builder.confirmTextColorStr;
        this.cancelTextColorStr = builder.cancelTextColorStr;
        
        this.defaultDistrict = builder.defaultDistrict;
        this.defaultCityName = builder.defaultCityName;
        this.defaultProvinceName = builder.defaultProvinceName;
        
        this.showProvinceAndCity = builder.showProvinceAndCity;
        this.backgroundPop = builder.backgroundPop;
        this.titleTextColorStr = builder.titleTextColorStr;
        this.dataLoader = CityLoader.singleInstance(context);
        
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        popview = layoutInflater.inflate(R.layout.pop_citypicker, null);
        
        mWheelViewProvince = (WheelView) popview.findViewById(R.id.id_province);
        mWheelViewCity = (WheelView) popview.findViewById(R.id.id_city);
        mWheelViewDistrict = (WheelView) popview.findViewById(R.id.id_district);
        mRelativeTitleBg = (RelativeLayout) popview.findViewById(R.id.rl_title);
        mTvOK = (TextView) popview.findViewById(R.id.tv_confirm);
        mTvTitle = (TextView) popview.findViewById(R.id.tv_title);
        mTvCancel = (TextView) popview.findViewById(R.id.tv_cancel);
        
        popwindow = new PopupWindow(popview, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popwindow.setBackgroundDrawable(new ColorDrawable(backgroundPop));
        popwindow.setAnimationStyle(R.style.AnimBottom);
        popwindow.setTouchable(true);
        popwindow.setOutsideTouchable(false);
        popwindow.setFocusable(true);
        
        /**
         * 设置标题背景颜色
         */
        if (!TextUtils.isEmpty(this.titleBackgroundColorStr)) {
            mRelativeTitleBg.setBackgroundColor(Color.parseColor(this.titleBackgroundColorStr));
        }
        
        /**
         * 设置标题
         */
        if (!TextUtils.isEmpty(this.mTitle)) {
            mTvTitle.setText(this.mTitle);
        }
        
        //设置确认按钮文字颜色
        if (!TextUtils.isEmpty(this.titleTextColorStr)) {
            mTvTitle.setTextColor(Color.parseColor(this.titleTextColorStr));
        }
        
        //设置确认按钮文字颜色
        if (!TextUtils.isEmpty(this.confirmTextColorStr)) {
            mTvOK.setTextColor(Color.parseColor(this.confirmTextColorStr));
        }
        
        //设置取消按钮文字颜色
        if (!TextUtils.isEmpty(this.cancelTextColorStr)) {
            mTvCancel.setTextColor(Color.parseColor(this.cancelTextColorStr));
        }
        
        //只显示省市两级联动
        if (this.showProvinceAndCity) {
            mWheelViewDistrict.setVisibility(View.GONE);
        }
        else {
            mWheelViewDistrict.setVisibility(View.VISIBLE);
        }

        // 添加change事件
        mWheelViewProvince.addChangingListener(this);
        // 添加change事件
        mWheelViewCity.addChangingListener(this);
        // 添加change事件
        mWheelViewDistrict.addChangingListener(this);
        // 添加onclick事件
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                hide();
            }
        });
        mTvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelected(mProvinceBean, mCityBean, mDistrictBean);
                hide();
            }
        });
        
    }
    
    public static class Builder {
        /**
         * Default text color
         */
        public static final int DEFAULT_TEXT_COLOR = 0xFF585858;
        
        /**
         * Default text size
         */
        public static final int DEFAULT_TEXT_SIZE = 18;
        
        // Text settings
        private int textColor = DEFAULT_TEXT_COLOR;
        
        private int textSize = DEFAULT_TEXT_SIZE;
        
        /**
         * 滚轮显示的item个数
         */
        private static final int DEF_VISIBLE_ITEMS = 5;
        
        // Count of visible items
        private int visibleItems = DEF_VISIBLE_ITEMS;
        
        /**
         * 省滚轮是否循环滚动
         */
        private boolean isProvinceCyclic = true;
        
        /**
         * 市滚轮是否循环滚动
         */
        private boolean isCityCyclic = true;
        
        /**
         * 区滚轮是否循环滚动
         */
        private boolean isDistrictCyclic = true;
        
        private Context mContext;
        
        /**
         * item间距
         */
        private int padding = 5;
        
        /**
         * Color.BLACK
         */
        private String cancelTextColorStr = "#000000";
        
        /**
         * Color.BLUE
         */
        private String confirmTextColorStr = "#0000FF";
        
        /**
         * 标题背景颜色
         */
        private String titleBackgroundColorStr = "#E9E9E9";
        
        /**
         * 标题颜色
         */
        private String titleTextColorStr = "#E9E9E9";
        
        /**
         * 第一次默认的显示省份，一般配合定位，使用
         */
        private String defaultProvinceName = "江苏";
        
        /**
         * 第一次默认得显示城市，一般配合定位，使用
         */
        private String defaultCityName = "常州";
        
        /**
         * 第一次默认得显示，一般配合定位，使用
         */
        private String defaultDistrict = "新北区";
        
        /**
         * 标题
         */
        private String mTitle = "选择地区";
        
        /**
         * 两级联动
         */
        private boolean showProvinceAndCity = false;
        
        /**
         * 设置popwindow的背景
         */
        private int backgroundPop = 0xa0000000;
        
        public Builder(Context context) {
            this.mContext = context;
        }
        
        /**
         * 设置popwindow的背景
         *
         * @param backgroundPopColor
         * @return
         */
        public Builder backgroundPop(int backgroundPopColor) {
            this.backgroundPop = backgroundPopColor;
            return this;
        }
        
        /**
         * 设置标题背景颜色
         *
         * @param colorBg
         * @return
         */
        public Builder titleBackgroundColor(String colorBg) {
            this.titleBackgroundColorStr = colorBg;
            return this;
        }
        
        /**
         * 设置标题背景颜色
         *
         * @param titleTextColorStr
         * @return
         */
        public Builder titleTextColor(String titleTextColorStr) {
            this.titleTextColorStr = titleTextColorStr;
            return this;
        }
        
        /**
         * 设置标题
         *
         * @param mtitle
         * @return
         */
        public Builder title(String mtitle) {
            this.mTitle = mtitle;
            return this;
        }
        
        /**
         * 是否只显示省市两级联动
         *
         * @param flag
         * @return
         */
        public Builder onlyShowProvinceAndCity(boolean flag) {
            this.showProvinceAndCity = flag;
            return this;
        }
        
        /**
         * 第一次默认的显示省份，一般配合定位，使用
         *
         * @param defaultProvinceName
         * @return
         */
        public Builder province(String defaultProvinceName) {
            this.defaultProvinceName = defaultProvinceName;
            return this;
        }
        
        /**
         * 第一次默认得显示城市，一般配合定位，使用
         *
         * @param defaultCityName
         * @return
         */
        public Builder city(String defaultCityName) {
            this.defaultCityName = defaultCityName;
            return this;
        }
        
        /**
         * 第一次默认地区显示，一般配合定位，使用
         *
         * @param defaultDistrict
         * @return
         */
        public Builder district(String defaultDistrict) {
            this.defaultDistrict = defaultDistrict;
            return this;
        }
        
        //        /**
        //         * 确认按钮文字颜色
        //         * @param color
        //         * @return
        //         */
        //        public Builder confirTextColor(int color) {
        //            this.confirmTextColor = color;
        //            return this;
        //        }
        
        /**
         * 确认按钮文字颜色
         *
         * @param color
         * @return
         */
        public Builder confirTextColor(String color) {
            this.confirmTextColorStr = color;
            return this;
        }
        
        //        /**
        //         * 取消按钮文字颜色
        //         * @param color
        //         * @return
        //         */
        //        public Builder cancelTextColor(int color) {
        //            this.cancelTextColor = color;
        //            return this;
        //        }
        
        /**
         * 取消按钮文字颜色
         *
         * @param color
         * @return
         */
        public Builder cancelTextColor(String color) {
            this.cancelTextColorStr = color;
            return this;
        }
        
        /**
         * item文字颜色
         *
         * @param textColor
         * @return
         */
        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }
        
        /**
         * item文字大小
         *
         * @param textSize
         * @return
         */
        public Builder textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }
        
        /**
         * 滚轮显示的item个数
         *
         * @param visibleItems
         * @return
         */
        public Builder visibleItemsCount(int visibleItems) {
            this.visibleItems = visibleItems;
            return this;
        }
        
        /**
         * 省滚轮是否循环滚动
         *
         * @param isProvinceCyclic
         * @return
         */
        public Builder provinceCyclic(boolean isProvinceCyclic) {
            this.isProvinceCyclic = isProvinceCyclic;
            return this;
        }
        
        /**
         * 市滚轮是否循环滚动
         *
         * @param isCityCyclic
         * @return
         */
        public Builder cityCyclic(boolean isCityCyclic) {
            this.isCityCyclic = isCityCyclic;
            return this;
        }
        
        /**
         * 区滚轮是否循环滚动
         *
         * @param isDistrictCyclic
         * @return
         */
        public Builder districtCyclic(boolean isDistrictCyclic) {
            this.isDistrictCyclic = isDistrictCyclic;
            return this;
        }
        
        /**
         * item间距
         *
         * @param itemPadding
         * @return
         */
        public Builder itemPadding(int itemPadding) {
            this.padding = itemPadding;
            return this;
        }
        
        public CityPickerView build() {
            CityPickerView cityPicker = new CityPickerView(this);
            return cityPicker;
        }
        
    }

    class AddressLoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            dataLoader.loadData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateAddressWheels();
            popwindow.showAtLocation(popview, Gravity.BOTTOM, 0, 0);
        }
    }
    private void setUpData() {
        new AddressLoadTask().execute();
    }

    private void updateAddressWheels() {
        int provinceDefault = -1;
        ProvinceBean[] provinceArray = dataLoader.getProvinceArray();
        if (!TextUtils.isEmpty(defaultProvinceName) && provinceArray.length > 0) {
            for (int i = 0; i < provinceArray.length; i++) {
                if (provinceArray[i].getName().contains(defaultProvinceName)) {
                    provinceDefault = i;
                    break;
                }
            }
        }
        ArrayWheelAdapter arrayWheelAdapter = new ArrayWheelAdapter<>(context, provinceArray, ArrayWheelAdapter.GRAVITY_LEFT);
        mWheelViewProvince.setViewAdapter(arrayWheelAdapter);
        //获取所设置的省的位置，直接定位到该位置
        if (-1 != provinceDefault) {
            mWheelViewProvince.setCurrentItem(provinceDefault);
        }
        // 设置可见条目数量
        mWheelViewProvince.setVisibleItems(visibleItems);
        mWheelViewCity.setVisibleItems(visibleItems);
        mWheelViewDistrict.setVisibleItems(visibleItems);
        mWheelViewProvince.setCyclic(isProvinceCyclic);
        mWheelViewCity.setCyclic(isCityCyclic);
        mWheelViewDistrict.setCyclic(isDistrictCyclic);
        arrayWheelAdapter.setPadding(padding);
        arrayWheelAdapter.setTextColor(textColor);
        arrayWheelAdapter.setTextSize(textSize);

        updateCities();
        updateAreas();
    }

    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        //省份滚轮滑动的当前位置
        int pCurrent = mWheelViewProvince.getCurrentItem();
        //省份选中的名称
        mProvinceBean = dataLoader.getProvinceArray()[pCurrent];
        
        CityBean[] cities = dataLoader.getProvinceCityMap().get(mProvinceBean.getName());
        if (cities == null) {
            return;
        }
        
        //设置最初的默认城市
        int cityDefault = -1;
        if (!TextUtils.isEmpty(defaultCityName) && cities.length > 0) {
            for (int i = 0; i < cities.length; i++) {
                if (defaultCityName.contains(cities[i].getName())) {
                    cityDefault = i;
                    break;
                }
            }
        }
        int cityTextGravity = showProvinceAndCity ? ArrayWheelAdapter.GRAVITY_RIGHT : Gravity.CENTER;
        ArrayWheelAdapter cityWheel = new ArrayWheelAdapter<CityBean>(context, cities, cityTextGravity);
        // 设置可见条目数量
        cityWheel.setTextColor(textColor);
        cityWheel.setTextSize(textSize);
        mWheelViewCity.setViewAdapter(cityWheel);
        if (-1 != cityDefault) {
            mWheelViewCity.setCurrentItem(cityDefault);
        }
        else {
            mWheelViewCity.setCurrentItem(0);
        }
        
        cityWheel.setPadding(padding);
        updateAreas();
    }
    
    /**
     * 根据当前的市，更新区WheelView的信息
     */
    private void updateAreas() {
        
        int pCurrent = mWheelViewCity.getCurrentItem();
        mCityBean = dataLoader.getProvinceCityMap().get(mProvinceBean.getName())[pCurrent];
        DistrictBean[] areas = dataLoader.getCityDistrictMap().get(mProvinceBean.getName() + mCityBean.getName());
        
        if (areas == null) {
            return;
        }
        
        int districtDefault = -1;
        if (!TextUtils.isEmpty(defaultDistrict) && areas.length > 0) {
            for (int i = 0; i < areas.length; i++) {
                if (defaultDistrict.contains(areas[i].getName())) {
                    districtDefault = i;
                    break;
                }
            }
        }
        
        ArrayWheelAdapter districtWheel = new ArrayWheelAdapter<DistrictBean>(context, areas, ArrayWheelAdapter.GRAVITY_RIGHT);
        // 设置可见条目数量
        districtWheel.setTextColor(textColor);
        districtWheel.setTextSize(textSize);
        mWheelViewDistrict.setViewAdapter(districtWheel);
        
        if (-1 != districtDefault) {
            mWheelViewDistrict.setCurrentItem(districtDefault);
            //获取第一个区名称
            mDistrictBean = dataLoader.getDistrictMap().get(mProvinceBean.getName() + mCityBean.getName() + defaultDistrict);
        }
        else {
            mWheelViewDistrict.setCurrentItem(0);
            if (areas.length > 0) {
                mDistrictBean = areas[0];
            }
        }
        districtWheel.setPadding(padding);
        
    }
    
    @Override
    public void setType(int type) {
    }
    
    @Override
    public void show() {
        if (!isShow()) {
            setUpData();
        }
    }
    
    @Override
    public void hide() {
        if (isShow()) {
            popwindow.dismiss();
        }
    }
    
    @Override
    public boolean isShow() {
        return popwindow.isShowing();
    }
    
    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mWheelViewProvince) {
            updateCities();
        }
        else if (wheel == mWheelViewCity) {
            updateAreas();
        }
        else if (wheel == mWheelViewDistrict) {
            mDistrictBean = dataLoader.getCityDistrictMap().get(mProvinceBean.getName() + mCityBean.getName())[newValue];
        }
    }
}
