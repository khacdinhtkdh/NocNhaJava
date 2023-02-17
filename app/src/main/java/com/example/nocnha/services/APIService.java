package com.example.nocnha.services;

import com.example.nocnha.Keys;
import com.example.nocnha.modelClass.DataSend;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key="+ Keys.TOKEN_KEY
    })
    @POST("fcm/send")
    Call<DataSend> sendNotification(@Body DataSend dataSend);
}
