package com.lt.jitpacktest.activity;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.utils.SessionSingleton;

public class YunBuRewardWebActivity extends AppCompatActivity {
    private Context context;
    private WebView wv_yunbu_reward;
    private String CLICKLINK;
    private ImageView iv_yunbu_reward_back;
    private TextView tv_yunbu_reward_title;
    private RelativeLayout rl_yunbu_reward_title_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_yun_bu_reward_web);

        Intent intent = getIntent();
        CLICKLINK = intent.getStringExtra("CLICKLINK");

        context = this;

        initview();
    }

    private void initview() {
        wv_yunbu_reward = findViewById(R.id.wv_yunbu_reward);
        iv_yunbu_reward_back = findViewById(R.id.iv_yunbu_reward_back);
        tv_yunbu_reward_title = findViewById(R.id.tv_yunbu_reward_title);
        rl_yunbu_reward_title_background = findViewById(R.id.rl_yunbu_reward_title_background);

        if (SessionSingleton.getInstance().hasStyleConfig == 1) {
            tv_yunbu_reward_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
            rl_yunbu_reward_title_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());


            if (SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon() == 0) {
                iv_yunbu_reward_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
            } else {
                iv_yunbu_reward_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
            }
        }


        // 清缓存和记录，缓存引起的白屏
        wv_yunbu_reward.clearCache(true);
        wv_yunbu_reward.clearHistory();

        wv_yunbu_reward.requestFocus();
        WebSettings webSettings = wv_yunbu_reward.getSettings();
        webSettings.setDatabaseEnabled(true);
        // 缓存白屏
        String appCachePath = getApplicationContext().getCacheDir()
                .getAbsolutePath() + "/webcache";
        // 设置 Application Caches 缓存目录
        webSettings.setAppCachePath(appCachePath);
        webSettings.setDatabasePath(appCachePath);


        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= 21) {
            setWebView1();
        } else {
            setWebView2();
        }

        wv_yunbu_reward.setWebChromeClient(new ReWebChomeClient());//选择图片的回调


        iv_yunbu_reward_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private ValueCallback<Uri[]> mValueCallback;

    //自定义WebChromeClient
    public class ReWebChomeClient extends WebChromeClient {


        public ReWebChomeClient() {
        }

        @Override


        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            tv_yunbu_reward_title.setText(title);//原生标题栏，显示h5对应页面的标题
        }

        @Override


        public void onProgressChanged(WebView view, int newProgress) {
            // TODO Auto-generated method stub
            super.onProgressChanged(view, newProgress);
        }

        //h5选择图片

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
            mValueCallback = valueCallback;//选择图片的回调
//选择图片前，务必保证应用获取了读写存储权限，否则影响选择图片//也可在此处获取读写存储权限，确认获取后再执行选择图片方法
            selectAlbum();
            return true;
        }
    }

    /**
     *  * 选择图片
     *  
     */
    public void selectAlbum() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1234);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            Uri tempUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(tempUri, filePathColumn, null,
                    null, null);
            if (cursor == null) {
                tempUri = getUri(this, data);
            }
            Uri[] uris = new Uri[1];
            uris[0] = tempUri;
            mValueCallback.onReceiveValue(uris);
            mValueCallback = null;
        } else {
            //重要！当用户关闭图库或未选择图片，也需要回调传null，否则第二次选择不了图片
            mValueCallback.onReceiveValue(null);
            mValueCallback = null;
        }
    }

    /**
     *  * 获取图库选择图片的uri
     *  
     */
    public static Uri getUri(Context context, Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();

                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");

                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                        Log.i("urishi", uri.toString());
                    }
                }
            }
        }
        return uri;
    }


    private void setWebView2() {

        wv_yunbu_reward.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); // Android默认的处理方式
                if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
                        || error.getPrimaryError() == SslError.SSL_EXPIRED
                        || error.getPrimaryError() == SslError.SSL_INVALID
                        || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {

                    handler.proceed();

                } else {
                    handler.cancel();
                }// 接受所有网站的证书
                //handleMessage(Message msg); // 进行其他处理
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不另跳浏览器
                // 在2.3上面不加这句话，可以加载出页面，在4.0上面必须要加入，不然出现白屏
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    //wv_jd_transit.stopLoading();
                    return true;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        wv_yunbu_reward.getSettings().setJavaScriptEnabled(true);
        wv_yunbu_reward.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        wv_yunbu_reward.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv_yunbu_reward.getSettings().setDomStorageEnabled(true);
        wv_yunbu_reward.getSettings().setDatabaseEnabled(true);
        wv_yunbu_reward.getSettings().setAppCacheEnabled(true);
        wv_yunbu_reward.getSettings().setAllowFileAccess(true);
        wv_yunbu_reward.getSettings().setSavePassword(true);
        wv_yunbu_reward.getSettings().setSupportZoom(true);
        wv_yunbu_reward.getSettings().setBuiltInZoomControls(true);
        wv_yunbu_reward.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        wv_yunbu_reward.getSettings().setUseWideViewPort(true);

        wv_yunbu_reward.loadUrl(CLICKLINK);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setWebView1() {

        wv_yunbu_reward.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); // Android默认的处理方式
                if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
                        || error.getPrimaryError() == SslError.SSL_EXPIRED
                        || error.getPrimaryError() == SslError.SSL_INVALID
                        || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {

                    handler.proceed();

                } else {
                    handler.cancel();
                }// 接受所有网站的证书

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不另跳浏览器
                // 在2.3上面不加这句话，可以加载出页面，在4.0上面必须要加入，不然出现白屏
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    //wv_jd_transit.stopLoading();
                    return true;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;

            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        wv_yunbu_reward.getSettings().setJavaScriptEnabled(true);
        wv_yunbu_reward.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        wv_yunbu_reward.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv_yunbu_reward.getSettings().setDomStorageEnabled(true);
        wv_yunbu_reward.getSettings().setDatabaseEnabled(true);
        wv_yunbu_reward.getSettings().setAppCacheEnabled(true);
        wv_yunbu_reward.getSettings().setAllowFileAccess(true);
        wv_yunbu_reward.getSettings().setSavePassword(true);
        wv_yunbu_reward.getSettings().setSupportZoom(true);
        wv_yunbu_reward.getSettings().setBuiltInZoomControls(true);
        wv_yunbu_reward.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        wv_yunbu_reward.getSettings().setUseWideViewPort(true);


        wv_yunbu_reward.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        wv_yunbu_reward.loadUrl(CLICKLINK);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //销毁Webview
    @Override
    protected void onDestroy() {
        if (wv_yunbu_reward != null) {
            wv_yunbu_reward.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            wv_yunbu_reward.clearHistory();

            ((ViewGroup) wv_yunbu_reward.getParent()).removeView(wv_yunbu_reward);
            wv_yunbu_reward.destroy();
            wv_yunbu_reward = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && wv_yunbu_reward.canGoBack()) {
            wv_yunbu_reward.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
