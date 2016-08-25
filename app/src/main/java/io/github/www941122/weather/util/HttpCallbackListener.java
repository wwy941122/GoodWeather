package io.github.www941122.weather.util;

/**
 * Created by Administrator on 2016/4/27 0027.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
