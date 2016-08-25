package io.github.www941122.weather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.www941122.weather.model.City;
import io.github.www941122.weather.model.Country;
import io.github.www941122.weather.model.GoodWeatherDB;
import io.github.www941122.weather.model.Province;

/**
 * Created by Administrator on 2016/4/26 0026.
 */
public class Utility {

    public synchronized static boolean handleProvincesResponse(GoodWeatherDB goodWeatherDB, String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");//����ѯ�����ַ����Զ���Ϊ��ֿ�
            if (allProvinces != null && allProvinces.length > 0){
                for(String p: allProvinces){//����������ÿ��Ԫ��
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    goodWeatherDB.saveProvince(province);
                }
                return true;
            }

        }
        return false;
    }

    public synchronized static boolean handleCitiesResponse(GoodWeatherDB goodWeatherDB, String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");//����ѯ�����ַ����Զ���Ϊ��ֿ�
            if (allCities != null && allCities.length > 0){
                for(String c: allCities){//����������ÿ��Ԫ��
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    goodWeatherDB.saveCity(city);
                }
                return true;
            }

        }
        return false;
    }

    public synchronized static boolean handleCountiesResponse(GoodWeatherDB goodWeatherDB, String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCountries = response.split(",");//����ѯ�����ַ����Զ���Ϊ��ֿ�
            if (allCountries != null && allCountries.length > 0){
                for(String c: allCountries){//����������ÿ��Ԫ��
                    String[] array = c.split("\\|");
                    Country country = new Country();
                    country.setCountryCode(array[0]);
                    country.setCountryName(array[1]);
                    country.setCityId(cityId);
                    goodWeatherDB.saveCountry(country);
                }
                return true;
            }

        }
        return false;
    }

    public static void handleWeatherResponse(Context context, String response){
        try {
            Log.d("utility", response);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName= weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            Log.d("json",weatherDesp);
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void saveWeatherInfo (Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy��m��d��", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }
}
