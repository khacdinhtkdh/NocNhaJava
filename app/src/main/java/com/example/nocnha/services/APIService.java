package com.example.nocnha.services;

import static com.example.nocnha.Constants.TOKEN_KEY;

import com.example.nocnha.modelClass.DataSend;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAs7yuGkw:APA91bGxke-l4xxuihGq1y8N63eJbUOD0HQ3yCOIb_sf2sQEpnpguvThAZ5SjIiid6meOsLfJKIF_fQdYiObyLNMeKVX4ST_9vMOEcAaqWQUQTmLwM1_T3_iLC77nvJo1Lj-19kTuLzX"
    })
    @POST("fcm/send")
    Call<DataSend> sendNotification(@Body DataSend dataSend);
}
