package com.kiere.pooldasenticx2;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraUtil {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static ImageCapture imageCapture;
    public static String sentiResult = "init_cameraUtl";

    public static void startCamera(Context context,PreviewView viewFinder) {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {

                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();

                // Set up the capture use case to allow users to take photos
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetResolution(new Size(1080, 1920))
                        .build();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // init camera
                cameraProvider.unbindAll();

                // Attach use cases to the camera with the same lifecycle owner
                cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) context),
                        cameraSelector,
                        preview,
                        imageCapture);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(viewFinder.createSurfaceProvider());

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(context));

    }

    public static void captureImage(Context context, PreviewView viewFinder, WebView webView){
        File file = new File(context.getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(context, "Image Saved successfully", Toast.LENGTH_SHORT).show();

                        //viewFinder.setVisibility(View.INVISIBLE);
                        // imageView.setVisibility(View.VISIBLE);

                        NetworkCall.sendSentiResult(webView,file);

                        // ????????? ????????? ??? ????????? ??????
//                        Map<String,String> sentiResultMap = NetworkCall.getSentiResult(file);
//                        String reqResult = sentiResultMap.get("reqResult");
//                        String sentiResult = sentiResultMap.get("sentiResult");
//
//                        Log.d("sentiResultMap", String.valueOf(sentiResultMap));
//                        Log.d("reqResult",reqResult);
//                        Log.d("sentiResult",sentiResult);
//
//                        if(Objects.equals(reqResult, "true")){
//                           webView.evaluateJavascript("javascript:callFromApp("+sentiResult+")", new ValueCallback<String>() {
//                              @Override
//                              public void onReceiveValue(String toast) {
//                                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
//                              }
//                           });
//
//                           // ???????????? ????????? ????????? ????????????
//                           //NetworkCall.sendPhotoToReqServer(file);
//                        }

                    }
                });
            }
            @Override
            public void onError(@NonNull ImageCaptureException error) {
                error.printStackTrace();
            }
        });

    }

}
