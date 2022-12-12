package com.kiere.pooldasenticx2;

import android.content.Context;

// 웹뷰 관련
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static WebView webView;
    private WebSettings webViewSetting;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    public final String webUrlLocal = "http://studyeng.peso.co.kr/#/";

    PreviewView mPreviewView;
    //ImageView captureImage;
    //ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        /* okhttp 동기전송시 Error 해결
           StrictMode$AndroidBlockGuardPolicy.onNetwork [duplicate]
           Thread 이슈
        */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mPreviewView = findViewById(R.id.previewView);
        //captureImage = findViewById(R.id.captureImg);
        //imageView =findViewById(R.id.imageView);

        initCamera();

        // 캡쳐 이미지
//        captureImage.setOnClickListener(v -> {
//           captureResult = CameraUtil.captureImage(this,mPreviewView, imageView);
//           Log.d("captureResult",captureResult);
//        });
    }

    private void initView() {
        webView = findViewById(R.id.mainWebview);

        webViewSetting = webView.getSettings();
        webViewSetting.setJavaScriptEnabled(true);
        webViewSetting.setDefaultTextEncodingName("utf-8");
        webViewSetting.setDomStorageEnabled(true);
        webViewSetting.setLoadWithOverviewMode(true);
        webViewSetting.setUseWideViewPort(true); // 페이지를 웹뷰 width에 맞춤
        webViewSetting.setSupportZoom(false); // 확대 비활성화
        webViewSetting.setBuiltInZoomControls(false); // 확대 비활성화

        webView.setWebChromeClient(new WebChromeClient() {
            //Context mContex = getApplicationContext();

            //debug :  logcat 패널에서 확인
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebConsole", consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            final Context mContext = getApplicationContext();

            @Override
            public void onPageFinished(WebView view, String url ) {

                super.onPageFinished(webView, url);
                Log.d("onPageFinished", "success" );

                //testJavaCallJs();
                //capturePhoto();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(mContext, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                Log.d("onReceivedError", description );
            }

        });

        //web javascript interface 추가
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.loadUrl(webUrlLocal);

    }
    public void testJavaCallJs() {
        webView.evaluateJavascript("javascript:callFromApp('key=val')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String toast) {
                Toast.makeText(getBaseContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 웹 호출 함수
    public static void callWebFromApp(String str){
         //webview thread 충돌회피 > post 사용
         webView.post(() -> webView.evaluateJavascript("javascript:callFromApp("+str+")", new ValueCallback<String>() {
             @Override
             public void onReceiveValue(String toast) {
                 //Toast.makeText(webView.getContext(), toast, Toast.LENGTH_SHORT).show();
             }

         }));
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Call from the web page : 웹에서 호출처리 함수 */
        @JavascriptInterface
        public void callFromWeb(String jsonStr) {
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                String obj = jObj.getString("obj"); // 대상
                String req = jObj.getString("req"); // 요청사항
                String msg = jObj.getString("msg"); // msg

                switch (obj) {
                    case "camera":
                        if (Objects.equals(req, "init")) initCamera(); // 카메라 초기화
                        break;
                    case "user":
                        if (Objects.equals(req, "senti")) doSentiAnalysis(); // 감정분석 처리
                        break;
                    case "app":
                        if (Objects.equals(req, "sysCheck")) appSysCheck(msg); // 감정분석 처리
                        break;
                }

            } catch (JSONException e) {
                Log.e("MYAPP", "unexpected JSON exception", e);
            }


       }
    }

    // app system 환경체크
    private void appSysCheck(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d("appSysCheck",msg);
    }

    // 카메라 초기화
    private void initCamera(){
        if(allPermissionsGranted()){
            CameraUtil.startCamera(this, mPreviewView); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        //Toast.makeText(this, "call initCamera.", Toast.LENGTH_SHORT).show();
        Log.d("call_initCamera","yes");
    }

    // 감정분석 처리
    private void doSentiAnalysis(){
        CameraUtil.captureImage(this, mPreviewView, webView);

        Log.d("call_doSentiAnalysis","yes");
        Toast.makeText(this, "감정분석이 시작되었습니다.", Toast.LENGTH_SHORT).show();

    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                CameraUtil.startCamera(this, mPreviewView); //start camera if permission has been granted by user
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
}