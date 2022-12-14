package com.kiere.pooldasenticx2;

import android.webkit.ValueCallback;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkCall {
    static final String url_senti = "https://www.bottalks.co.kr/?m=bbs&a=get_sentiResult"; // 감정분석 서버
    static final String url_clitent = "http://studyeng.peso.co.kr/api/saveSentiPhoto"; // 감정분석 요청 client
    static OkHttpClient client = new OkHttpClient();
    static Gson gson = new Gson();

    // 감정분석 서버에 파일 전송
    public static Map<String,String> fileUpload(File file) {
        String reqResult = "false";
        String sentiResult = "null";

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody).build();

        Request request = new Request.Builder().url(url_senti).post(multipartBody)
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
        Map<String,String> result = new HashMap<>();
        result.put("reqResult", reqResult);
        result.put("sentiResult",sentiResult);
        return result;
    }

    // 감정분석 요청한 서버에게 원본파일 전송
    public static void sendPhotoToReqServer(File file) {

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody).build();

        Request request = new Request.Builder().url(url_clitent).post(multipartBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    String res = body.string();

                    Log.v("sendPhoto", res);
                } else {
                    Log.e("ERROR_sendPhoto", "Failed to upload");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ERROR", "Failed to upload");
            }
        });

    }

    // 감정분석 요청한 서버에게 감정분석 결과값 전송 : webview js 방식 사용
    public static void sendSentiResult(WebView webView, File file) {

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody).build();

        Request request = new Request.Builder().url(url_senti).post(multipartBody)
                .addHeader("Content-Type","multipart/form-data")
                .addHeader("Authorization","Persona_AK 9cda130decb7a810713b2f9e18ceb03e")
                .build();

        // 비동기로 전송
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    String sentiResult = body.string();

                    // api 결과 매핑
                    JSONObject apiResult = new JSONObject();
                    try {
                        apiResult.put("obj", "user");
                        apiResult.put("req", "senti");
                        apiResult.put("result", sentiResult);
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    // 최종 리턴값
                    String apiResult_jsonStr = apiResult.toString();
                    Log.d("apiResult_jsonStr", apiResult_jsonStr);

                    MainActivity.callWebFromApp(apiResult_jsonStr);
                    //Toast.makeText(webView.getContext(), apiResult_jsonStr, Toast.LENGTH_SHORT).show();

                    // webview thread 회피
//                     webView.post(() -> webView.evaluateJavascript("javascript:callFromApp("+apiResult_jsonStr+")", new ValueCallback<String>() {
//                         @Override
//                         public void onReceiveValue(String toast) {
//                             //Toast.makeText(webView.getContext(), toast, Toast.LENGTH_SHORT).show();
//                         }
//
//                     }));

//                    webView.evaluateJavascript("javascript:callFromApp("+apiResult_jsonStr+")", new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String value) {
//
//                        }
//                    });
                    Log.v("sentiResult", sentiResult);
                } else {
                    Log.e("ERROR_sendPhoto", "Failed to upload");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ERROR", "Failed to upload");
            }
        });

    }

    // 감정분석 결과값 리턴 : 사진외에 영상으로 할 수도 있어서 별도 메서드로 처리
    public static Map<String,String> getSentiResult(File file){
         Map<String,String> fileUploadResult = fileUpload(file);

         return fileUploadResult;
    }

}
