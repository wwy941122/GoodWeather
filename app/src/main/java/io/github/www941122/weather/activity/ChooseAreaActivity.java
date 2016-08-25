package io.github.www941122.weather.activity;

import android.app.ProgressDialog;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.github.www941122.weather.*;

import java.util.ArrayList;
import java.util.List;

import io.github.www941122.weather.R;
import io.github.www941122.weather.model.City;
import io.github.www941122.weather.model.Country;
import io.github.www941122.weather.model.GoodWeatherDB;
import io.github.www941122.weather.model.Province;
import io.github.www941122.weather.util.HttpCallbackListener;
import io.github.www941122.weather.util.HttpUtil;
import io.github.www941122.weather.util.Utility;

public class ChooseAreaActivity extends ActionBarActivity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private GoodWeatherDB goodWeatherDB;
    private List<String> datalist = new ArrayList<String>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private boolean isFromWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        isFromWeather = getIntent().getBooleanExtra("isFromWeather",false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected", false) && isFromWeather == false){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }



        setContentView(R.layout.activity_choose_area);

        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);

        goodWeatherDB = GoodWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCountries();
                } else if (currentLevel == LEVEL_COUNTRY){
                    String countryCode = countryList.get(position).getCountryCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("country_code", countryCode);
                    startActivity(intent);
                    finish();

                }
            }
        });
        queryProvinces();


    }
    /*
    * 查询全国所有的省，优先从数据库查，没有就去服务器查询
    *
    * */
    private void queryProvinces(){
        provinceList = goodWeatherDB.loadProvince();
        if(provinceList.size() > 0){  // 从数据库中获取
            datalist.clear();
            for(Province province : provinceList){
                datalist.add(province.getProvinceName());
            }
            Log.d("加载成功！", datalist.toString());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            String s = new String("中国");
            titleText.setText(s);
            currentLevel =  LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");

        }

    }

    private void queryCities(){
        cityList = goodWeatherDB.loadCity(selectedProvince.getId());
        if(cityList.size() > 0){  // 从数据库中获取
            datalist.clear();
            for(City city: cityList){
                datalist.add(city.getCityName());
            }
            Log.d("加载成功！",datalist.toString());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel =  LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");

        }

    }

    private void queryCountries(){
        countryList = goodWeatherDB.loadCountry(selectedCity.getId());
        if(countryList.size() > 0){  // 从数据库中获取
            datalist.clear();
            for(Country country: countryList){
                datalist.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel =  LEVEL_COUNTRY;
        }else {
            queryFromServer(selectedCity.getCityCode(),"country");

        }

    }

    private void queryFromServer(final String code, final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(goodWeatherDB,response);
                } else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(goodWeatherDB,response,selectedProvince.getId());
                } else if("country".equals(type)){
                    result = Utility.handleCountiesResponse(goodWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            } else if("city".equals(type)){
                                queryCities();
                            } else if("country".equals(type)){
                                queryCountries();

                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(currentLevel == LEVEL_COUNTRY){
            queryCities();
        } else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        } else {
            finish();
        }
    }
}
