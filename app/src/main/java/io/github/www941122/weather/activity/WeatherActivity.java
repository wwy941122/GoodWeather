package io.github.www941122.weather.activity;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.www941122.weather.R;
import io.github.www941122.weather.util.HttpCallbackListener;
import io.github.www941122.weather.util.HttpUtil;
import io.github.www941122.weather.util.Utility;

public class WeatherActivity extends ActionBarActivity {
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;//城市名字
    private TextView publishText;//发布时间
    private TextView weatherDespText;//天气描述信息
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDataText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDataText = (TextView) findViewById(R.id.current_date);
        String countryCode = getIntent().getStringExtra("country_code");

        if(!TextUtils.isEmpty(countryCode)){
            publishText.setText(new String("正在加载..."));
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        } else {
            showWeather();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,ChooseAreaActivity.class);
        intent.putExtra("isFromWeather",true);
        startActivity(intent);
        finish();

    }

    /*
        * 查询县级代号对应的天气代号
        * */
    private void queryWeatherCode(String countryCode){
        String address = "http://www.weather.com.cn/data/list3/city" + countryCode  + ".xml";
        queryFromServer(address, "countryCode");
    }
        /*
    * 查询天气代号对应的天气
    * */

    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode  + ".html";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countryCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            Log.d("weathercode",weatherCode);
                            queryWeatherInfo(weatherCode);
                        }
                        }
                    } else if ("weatherCode".equals(type)) {
                    Log.d("queryfromserver", response);
                    Utility.handleWeatherResponse(WeatherActivity.this, response);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });

                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });

            }
        });

    }
    /*
    * 从SharePreference文件中读取存储的天气信息，并显示
    * */
    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("sharepreferences",prefs.getString("weather_desp", ""));
        cityNameText.setText(prefs.getString("city_name", ""));

        temp1Text.setText (prefs.getString("temp1", ""));
        temp2Text.setText (prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今日" + prefs.getString("publish_time", "" )+ "发布");
        currentDataText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }



}
