package com.example.dog_breed_app;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("predict")
    Call<PredictionResponse> predictDogBreed(@Part MultipartBody.Part image);
}