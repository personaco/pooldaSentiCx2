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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
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

                testJavaCallJs();
                capturePhoto();
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

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    // 카메라 초기화
    private void initCamera(){
        if(allPermissionsGranted()){
            CameraUtil.startCamera(this, mPreviewView); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    // 캡쳐 사진
    private void capturePhoto(){
        CameraUtil.captureImage(this, mPreviewView, webView);
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