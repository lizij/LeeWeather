package com.lizij.cocoweather.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lizij.cocoweather.activity.WeatherActivity;
import com.lizij.cocoweather.application.AppApplication;
import com.lizij.cocoweather.R;
import com.lizij.cocoweather.db.City;
import com.lizij.cocoweather.db.County;
import com.lizij.cocoweather.db.Province;
import com.lizij.cocoweather.util.HttpUtil;
import com.lizij.cocoweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private Province currentProvince;

    private List<City> cityList;
    private City currentCity;

    private List<County> countyList;
    private County currentCounty;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        currentProvince = provinceList.get(position);
                        queryCity();
                        break;
                    case LEVEL_CITY:
                        currentCity = cityList.get(position);
                        queryCounty();
                        break;
                    case LEVEL_COUNTY:
                        currentCounty = countyList.get(position);
                        String countyCode = currentCounty.getCountyCode();
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.swipeRefreshLayout.setRefreshing(true);
                        weatherActivity.countyCode = countyCode;
                        weatherActivity.requestWeather(countyCode);
                        break;
                    default:
                        break;
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentLevel){
                    case LEVEL_CITY:
                        queryProvince();
                        break;
                    case LEVEL_COUNTY:
                        queryCity();
                        break;
                    default:
                        break;
                }
            }
        });

        queryProvince();
    }

    private void queryProvince(){
        titleText.setText("选择省份");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            updateCityList();
        }
    }

    private void queryCity(){
        titleText.setText(currentProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceCode = ?", String.valueOf(currentProvince.getProvinceCode())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            updateCityList();
        }
    }

    private void queryCounty(){
        titleText.setText(currentCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityCode = ?", String.valueOf(currentCity.getCityCode())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            updateCityList();
        }
    }

    private void updateCityList(){
        showProgressDialog();
        String CITY_LIST_ADDRESS = AppApplication.getProperties().getProperty("CITY_LIST_ADDRESS");
        HttpUtil.sendOkHttpRequest(CITY_LIST_ADDRESS, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(Utility.handleCityListResponse(response.body().string())){
                    Utility.checkDatabase();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            queryProvince();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "更新失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在更新城市列表");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
