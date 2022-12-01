package com.kiere.pooldasenticx2;

import android.util.Log;
import androidx.annotation.NonNull;
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

public class NetworkCall {
    static String url = "https://www.bottalks.co.kr/?m=bbs&a=get_sentiResult"; // TODO: Replace URL

    static OkHttpClient client = new OkHttpClient();

    public static void fileUpload(File file) {

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody).build();

        Request request = new Request.Builder().url(url).post(multipartBody)
                .addHeader("Content-Type","multipart/form-data")
                .addHeader("Authorization","Persona_AK 9cda130decb7a810713b2f9e18ceb03e")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    String res = body.string();
                    Log.v("RESPONSE", res);
                } else {
                    Log.e("ERROR", "Failed to upload");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ERROR", "Failed to upload");
            }
        });
    }

}
