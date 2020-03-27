package com.easemob.whiteboard;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;


public class MainActivity extends Activity {

    private String roomUrl;
    private RelativeLayout back;

    public ValueCallback<Uri[]> mUploadMessage;

    public final static int FIREHOUSE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        roomUrl = getIntent().getExtras().getString("roomUrl");
        initView();
    }

    private void initView(){

        back = findViewById(R.id.back_layout);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        WebView webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        //webview.addJavascriptInterface();//本地java对象映射给js调用
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置可以支持缩放
        webview.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        webview.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        webview.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webview.loadUrl(roomUrl);

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(new Runnable(){
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
                openFileChooser(uploadMsg, fileChooserParams);
                return true;
            }
        });
    }
    private void openFileChooser(ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
        mUploadMessage = uploadMsg;
//        Intent intent = fileChooserParams.createIntent();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, FIREHOUSE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FIREHOUSE_RESULT_CODE) {
            if (null == mUploadMessage)
                return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            if (result != null) {
                mUploadMessage.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessage.onReceiveValue(new Uri[]{});
            }
            mUploadMessage = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
