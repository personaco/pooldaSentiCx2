package com.kiere.pooldasenticx2;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Call;
import okhttp3.Callback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkCall {
    static String url = "https://www.bottalks.co.kr/?m=bbs&a=get_sentiResult";
    static OkHttpClient client = new OkHttpClient();
    static Gson gson = new Gson();

    public static Map<String,String> fileUpload(File file) {
        String reqResult = "false";
        String sentiResult = "null";

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody).build();

        Request request = new Request.Builder().url(url).post(multipartBody)
                .addHeader("Content-Type","multipart/form-data")
                .addHeader("Authorization","Persona_AK 9cda130decb7a810713b2f9e18ceb03e")
                .build();

        // 동기식으로 보낸다
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                ResponseBody body = response.body();
                if (body != null) {
                    String sentiResponse = body.string();
                    Log.v("RESPONSE", sentiResponse);
                    reqResult = "true";
                    sentiResult = sentiResponse;
                    body.close();
                } else {
                    String fail_msg = "Response Body is null";
                    Log.e("ERROR", fail_msg);
                    reqResult = "false";
                    sentiResult = fail_msg;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            reqResult = "false";
            sentiResult = "Fail to upload";

        }
        Map<String,String> result = new HashMap<String,String>();
        result.put("reqResult", reqResult);
        result.put("sentiResult",sentiResult);
        return result;
    }

    public static void sendPhotoToReqServer(File file) {
        String reqResult = "false";
        String sentiResult = "null";

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody).build();

        Request request = new Request.Builder().url(url).post(multipartBody)
                .addHeader("Content-Type","multipart/form-data")
                .addHeader("Authorization","Persona_AK 9cda130decb7a810713b2f9e18ceb03e")
                .build();

        // 동기식으로 보낸다
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                ResponseBody body = response.body();
                if (body != null) {
                    String sentiResponse = body.string();
                    Log.v("RESPONSE", sentiResponse);
                    reqResult = "true";
                    sentiResult = sentiResponse;
                    body.close();
                } else {
                    String fail_msg = "Response Body is null";
                    Log.e("ERROR", fail_msg);
                    reqResult = "false";
                    sentiResult = fail_msg;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            reqResult = "false";
            sentiResult = "Fail to upload";

        }

    }

    // 감정분석 결과값 리턴 : 사진외에 영상으로 할 수도 있어서 별도 메서드로 처리
    public static Map<String,String> getSentiResult(File file){
         Map<String,String> fileUploadResult = fileUpload(file);

         return fileUploadResult;
    }

}
