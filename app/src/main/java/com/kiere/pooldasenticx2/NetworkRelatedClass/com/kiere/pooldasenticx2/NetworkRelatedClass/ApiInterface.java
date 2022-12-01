package com.kiere.pooldasenticx2.NetworkRelatedClass;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface ApiInterface {
    @Headers({
      "Content-Type: multipart/form-data",
      "Authorization: Persona_AK 9cda130decb7a810713b2f9e18ceb03e"
    })
    @Multipart
    //@POST("face-api")
    @POST("?m=bbs&a=get_sentiResult")
    Call<ResponseModel> fileUpload(@Part MultipartBody.Part file);

}
