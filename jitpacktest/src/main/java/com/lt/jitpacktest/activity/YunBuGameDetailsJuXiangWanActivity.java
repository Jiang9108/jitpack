package com.lt.jitpacktest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.service.DownloadService;
import com.lt.jitpacktest.utils.HttpUtils;
import com.lt.jitpacktest.utils.SessionSingleton;
import com.lt.jitpacktest.utils.Utils;
import com.lt.jitpacktest.xPullRefresh.XListView;
import com.lt.jitpacktest.yunbuimageload.AsyncImageLoader;
import com.lt.jitpacktest.yunbuimageload.FileCache;
import com.lt.jitpacktest.yunbuimageload.MemoryCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class YunBuGameDetailsJuXiangWanActivity extends AppCompatActivity {
    private XListView xlv_gamedetails;
    private TextView tv_gamedetails_start_game;
    private LinearLayout ll_gamedetails_qq, ll_gamedetails_gonglue;
    private LinearLayout ll_gamedetails_start_game;
    private RelativeLayout ll_gamedetails_load_game;
    private ProgressBar pb_gamedetails;
    private PopupWindow tosatPopuo, tishiPopuo;

    private TextView tv_game_details_item_type_one, tv_game_details_item_type_two, tv_game_details_item_type_three, tv_game_details_item_type_four;
    private LinearLayout ll_game_details_item_type_one, ll_game_details_item_type_two, ll_game_details_item_type_three, ll_game_details_item_type_four;

    private TextView tv_game_details_item_type_one_zhishiqi, tv_game_details_item_type_two_zhishiqi, tv_game_details_item_type_three_zhishiqi, tv_game_details_item_type_four_zhishiqi;

    private Context context;
    public Dialog mLoading;

    private JSONArray taskArray;
    private JSONObject gameListData;

    private GameDetailsAdapter adapter;
    private TypeThreeAdapter typeThreeAdapter;

    private String DownUrl, account, msaoaid, packgeName, fileUrl = "/storage/emulated/0/game.apk";
    private String juxiangwanId;
    private int isOpenChecked = 1, isFirstShow = 0, isOpenRules = 0;
    private String ListType = "1";
    private JSONArray TypeThreeArray;

    private String ShowFirst;

    private String sys_ver, imei1, juxiangwanSgin;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View layout;


    int isRefresh = 0;

    long StopTime = 0L;

    String kefuQQ;


    private boolean isBindService;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
            DownloadService downloadService = binder.getService();

            //接口回调，下载进度
            downloadService.setOnProgressListener(new DownloadService.OnProgressListener() {
                @Override
                public void onProgress(float fraction) {
                    Log.i("", "下载进度：" + fraction);
                    //int num=(int)(fraction*100);

                    int progress = Math.round(fraction);
                    //float progress=fraction/100;

                    //wv_gamedetails.setWaveLevelRatio(progress);
                    pb_gamedetails.setProgress(progress);

                    //判断是否真的下载完成进行安装了，以及是否注册绑定过服务
                    if (fraction == DownloadService.UNBIND_SERVICE && isBindService) {
                        context.unbindService(conn);
                        isBindService = false;
                        Utils.showToast(context, "下载完成！");

                        tv_gamedetails_start_game.setText("开始任务");
                        ll_gamedetails_start_game.setVisibility(View.VISIBLE);
                        ll_gamedetails_load_game.setVisibility(View.GONE);

                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_yun_bu_game_details);

        context = this;

        Intent intent = getIntent();
        juxiangwanId = intent.getStringExtra("ADID");
        ShowFirst = intent.getStringExtra("ShowFirst");
        kefuQQ = intent.getStringExtra("KeFuQQ");

        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(true);
        mLoading.show();

        SessionSingleton.getInstance().mysp = getSharedPreferences("limit",Activity.MODE_PRIVATE);

        initview();
        setlistener();
    }


    private void setlistener() {
        ll_gamedetails_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openQQChat(context, kefuQQ);
                showToast(0, "");
            }
        });
        ll_gamedetails_gonglue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");//打开手机自带浏览器
                intent.setData(Uri.parse("https://www.baidu.com"));//设置
                //需要打开的网址
                startActivity(intent);
            }
        });
        ll_gamedetails_start_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                long StartTime = System.currentTimeMillis();

                //5分钟后结束
                if (StopTime - StartTime > 300000) {
                    showToastTISHI();
                } else {
                    Utils.showToast(context, "游戏即将结束，或已经到期！");
                    try {
                        String limitgame = SessionSingleton.getInstance().mysp.getString("limitgame", "") + gameListData.getJSONObject("base_info").getString("name");
                        SessionSingleton.getInstance().mysp.edit().putString("limitgame", limitgame).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }
        });
    }

    private void initview() {
        gameListData = new JSONObject();
        taskArray = new JSONArray();
        TypeThreeArray = new JSONArray();

        xlv_gamedetails = (XListView) findViewById(R.id.xlv_gamedetails);
        tv_gamedetails_start_game = (TextView) findViewById(R.id.tv_gamedetails_start_game);

        ll_gamedetails_qq = (LinearLayout) findViewById(R.id.ll_gamedetails_qq);
        ll_gamedetails_gonglue = (LinearLayout) findViewById(R.id.ll_gamedetails_gonglue);

        ll_gamedetails_start_game = (LinearLayout) findViewById(R.id.ll_gamedetails_start_game);
        ll_gamedetails_load_game = (RelativeLayout) findViewById(R.id.rl_gamedetails_load_game);

        pb_gamedetails = (ProgressBar) findViewById(R.id.yunbu_pb_gamedetails);

        try {
            account = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");
            msaoaid = SessionSingleton.getInstance().AccountSingle.getString("oaId");
            imei1 = Utils.getIMEI(context, 1);

            juxiangwanSgin = Utils.getMD5(SessionSingleton.getInstance().JuXiangWanSingle.getString("key") + account + SessionSingleton.getInstance().JuXiangWanSingle.getString("secret"));

            ListType = "1";
            getJuXiangWanGameDetails(juxiangwanId);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        xlv_gamedetails.setPullRefreshEnable(true);
        xlv_gamedetails.setPullLoadEnable(false);
        adapter = new GameDetailsAdapter(context);
        xlv_gamedetails.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                mLoading.show();

                getJuXiangWanGameDetails(juxiangwanId);

                xlv_gamedetails.stopRefresh();
            }

            @Override
            public void onLoadMore() {
                xlv_gamedetails.stopLoadMore();
            }

        });

        xlv_gamedetails.setAdapter(adapter);

    }

    //聚享玩游戏获取token
    private void getJuXiangWanGameDetails(String ad_id) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                Map<String, String> params = new HashMap<>();
                params.put("ad_id", ad_id);
                params.put("sysver", "10");
                params.put("resource_id", account);
                params.put("mid", SessionSingleton.getInstance().JuXiangWanSingle.getString("key"));
                params.put("oaid", msaoaid);
                params.put("sign", juxiangwanSgin);
                HttpUtils.dogetHttpReqeust("http://api.juxiangwan.com/home/interface/detail?", params, new HttpUtils.StringCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject returnJSONObject = new JSONObject(response);
                            if (returnJSONObject.getInt("code") == 1) {
                                gameListData = returnJSONObject.getJSONObject("data");
                                if (gameListData.getJSONObject("task_detail").has("play")) {
                                    taskArray = gameListData.getJSONObject("task_detail").getJSONObject("play").getJSONArray("list");
                                } else {
                                    showToast(1, "该游戏暂时无试玩奖励！请更换游戏");

                                    String limitgame = SessionSingleton.getInstance().mysp.getString("limitgame", "") + gameListData.getJSONObject("base_info").getString("name");
                                    SessionSingleton.getInstance().mysp.edit().putString("limitgame", limitgame).commit();
                                }

                            } else {
                                Utils.showToast(context, returnJSONObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        adapter.notifyDataSetChanged();
                        mLoading.dismiss();
                    }

                    @Override
                    public void onFaileure(int code, Exception e) {
                        e.printStackTrace();

                    }
                });


            } else {

                Map<String, String> params = new HashMap<>();
                params.put("ad_id", ad_id);
                params.put("sysver", "9");
                params.put("resource_id", account);
                params.put("mid", SessionSingleton.getInstance().JuXiangWanSingle.getString("key"));
                params.put("device", imei1);
                params.put("sign", juxiangwanSgin);
                HttpUtils.dogetHttpReqeust("http://api.juxiangwan.com/home/interface/detail?", params, new HttpUtils.StringCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject returnJSONObject = new JSONObject(response);
                            if (returnJSONObject.getInt("code") == 1) {
                                gameListData = returnJSONObject.getJSONObject("data");
                                if (gameListData.getJSONObject("task_detail").has("play")) {
                                    taskArray = gameListData.getJSONObject("task_detail").getJSONObject("play").getJSONArray("list");
                                } else {
                                    finish();
                                    Toast.makeText(context, "该游戏暂时无试玩奖励！请更换游戏 ", Toast.LENGTH_SHORT).show();

                                    String limitgame = SessionSingleton.getInstance().mysp.getString("limitgame", "") + gameListData.getJSONObject("base_info").getString("name");
                                    SessionSingleton.getInstance().mysp.edit().putString("limitgame", limitgame).commit();
                                }
                            } else {
                                Utils.showToast(context, returnJSONObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        adapter.notifyDataSetChanged();
                        mLoading.dismiss();
                    }

                    @Override
                    public void onFaileure(int code, Exception e) {
                        e.printStackTrace();

                    }
                });

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class GameDetailsAdapter extends BaseAdapter {
        private AsyncImageLoader imageLoader;//异步组件
        private LayoutInflater inflater;


        public GameDetailsAdapter(Context context) {
            //inflater = LayoutInflater.from(context);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            MemoryCache mcache = new MemoryCache();//内存缓存
            String paht = getApplicationContext().getFilesDir().getAbsolutePath();
            File cacheDir = new File(paht, "yunbucache");//缓存根目录
            FileCache fcache = new FileCache(context, cacheDir, "yunbuimage");//文件缓存
            imageLoader = new AsyncImageLoader(context, mcache, fcache);
        }

        @Override
        public int getCount() {
            if (ListType.equals("1")) {
                if (ShowFirst.equals("none")) {
                    return taskArray.length();
                } else {
                    return taskArray.length() + 1;
                }
            } else {
                return taskArray.length() + 1;
            }
        }

        @Override
        public Object getItem(int position) {

            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final ViewTopHolder topHolder;
            final ViewTypeTwoHolder viewTypeTwoHolder;

            int type = getItemViewType(position);
            if (type == 0) {
                if (convertView == null) {
                    topHolder = new ViewTopHolder();
                    convertView = inflater.inflate(R.layout.item_yun_bu_game_details_top, null);

                    topHolder.ll_game_details_title_background = convertView.findViewById(R.id.ll_game_details_title_background);
                    topHolder.iv_game_details_back = convertView.findViewById(R.id.iv_game_details_back);
                    topHolder.iv_game_details_image = convertView.findViewById(R.id.iv_game_details_image);
                    topHolder.tv_game_details_title = convertView.findViewById(R.id.tv_game_details_title);
                    topHolder.tv_game_details_name = convertView.findViewById(R.id.tv_game_details_name);
                    topHolder.tv_game_details_size = convertView.findViewById(R.id.tv_game_details_size);
                    topHolder.tv_game_details_money = convertView.findViewById(R.id.tv_game_details_money);

                    topHolder.tv_game_details_time_one = convertView.findViewById(R.id.tv_game_details_time_one);
                    topHolder.tv_game_details_time_two = convertView.findViewById(R.id.tv_game_details_time_two);
                    topHolder.tv_game_details_time_three = convertView.findViewById(R.id.tv_game_details_time_three);
                    topHolder.tv_game_details_time_four = convertView.findViewById(R.id.tv_game_details_time_four);

                    topHolder.tv_game_details_account_msg_open_down = convertView.findViewById(R.id.tv_game_details_account_msg_open_down);
                    topHolder.iv_game_details_account_msg_open_down = convertView.findViewById(R.id.iv_game_details_account_msg_open_down);
                    topHolder.ll_game_details_account_msg_rules = convertView.findViewById(R.id.ll_game_details_account_msg_rules);

                    topHolder.ll_game_details_registered = convertView.findViewById(R.id.ll_game_details_registered);

                    topHolder.iv_game_details_registered_refresh = convertView.findViewById(R.id.iv_game_details_registered_refresh);

                    topHolder.tv_game_details_registered_title = convertView.findViewById(R.id.tv_game_details_registered_title);
                    topHolder.tv_game_details_registered_cant_msg = convertView.findViewById(R.id.tv_game_details_registered_cant_msg);

                    topHolder.tv_game_details_registered_msg_gameid = convertView.findViewById(R.id.tv_game_details_registered_msg_gameid);
                    topHolder.tv_game_details_registered_msg_gamename = convertView.findViewById(R.id.tv_game_details_registered_msg_gamename);
                    topHolder.tv_game_details_registered_msg_taskone = convertView.findViewById(R.id.tv_game_details_registered_msg_taskone);
                    topHolder.tv_game_details_registered_msg_tasktwo = convertView.findViewById(R.id.tv_game_details_registered_msg_tasktwo);
                    topHolder.tv_game_details_registered_msg_taskthree = convertView.findViewById(R.id.tv_game_details_registered_msg_taskthree);
                    topHolder.tv_game_details_registered_msg_taskfour = convertView.findViewById(R.id.tv_game_details_registered_msg_taskfour);
                    topHolder.tv_game_details_registered_msg_taskfive = convertView.findViewById(R.id.tv_game_details_registered_msg_taskfive);

                    topHolder.tv_game_details_rules_qq = convertView.findViewById(R.id.tv_game_details_rules_qq);

                    topHolder.tv_game_details_shiwanwanfei = convertView.findViewById(R.id.tv_game_details_shiwanwanfei);


                    tv_game_details_item_type_one = convertView.findViewById(R.id.tv_game_details_item_type_one);
                    tv_game_details_item_type_two = convertView.findViewById(R.id.tv_game_details_item_type_two);
                    tv_game_details_item_type_three = convertView.findViewById(R.id.tv_game_details_item_type_three);
                    tv_game_details_item_type_four = convertView.findViewById(R.id.tv_game_details_item_type_four);

                    tv_game_details_item_type_one_zhishiqi = convertView.findViewById(R.id.tv_game_details_item_type_one_zhishiqi);
                    tv_game_details_item_type_two_zhishiqi = convertView.findViewById(R.id.tv_game_details_item_type_two_zhishiqi);
                    tv_game_details_item_type_three_zhishiqi = convertView.findViewById(R.id.tv_game_details_item_type_three_zhishiqi);
                    tv_game_details_item_type_four_zhishiqi = convertView.findViewById(R.id.tv_game_details_item_type_four_zhishiqi);

                    ll_game_details_item_type_one = convertView.findViewById(R.id.ll_game_details_item_type_one);
                    ll_game_details_item_type_two = convertView.findViewById(R.id.ll_game_details_item_type_two);
                    ll_game_details_item_type_three = convertView.findViewById(R.id.ll_game_details_item_type_three);
                    ll_game_details_item_type_four = convertView.findViewById(R.id.ll_game_details_item_type_four);


                    convertView.setTag(topHolder);
                } else {
                    topHolder = (ViewTopHolder) convertView.getTag();
                }
                try {

                    topHolder.tv_game_details_rules_qq.setText("3、客服-如果有疑问，请截图该页面，并联系客服QQ：" + kefuQQ + "，工作时间（工作日9：00-18：00）");


                    JSONObject array = gameListData.getJSONObject("task_detail");
                    int status = gameListData.getJSONObject("button").getInt("status");
                    JSONArray accountArray = gameListData.getJSONObject("user_info").getJSONArray("play_info");

                    tv_gamedetails_start_game.setText(gameListData.getJSONObject("button").getString("title"));
                    DownUrl = gameListData.getJSONObject("base_info").getString("down_url");
                    packgeName = gameListData.getJSONObject("base_info").getString("package_name");

                    //按钮状态，status	number	状态 1可试玩 0不可试玩
                    if (status == 1) {
                        if (accountArray.length() == 0) {
                            topHolder.tv_game_details_registered_title.setText("尚未注册");
                            topHolder.tv_game_details_registered_cant_msg.setVisibility(View.VISIBLE);
                            topHolder.ll_game_details_registered.setVisibility(View.GONE);

                            ll_gamedetails_start_game.setEnabled(true);
                            topHolder.tv_game_details_registered_cant_msg.setText("点击开始试玩，注册账号试玩拿奖励~");
                        } else {
                            topHolder.tv_game_details_registered_title.setText("注册成功");
                            topHolder.tv_game_details_registered_cant_msg.setVisibility(View.GONE);
                            topHolder.ll_game_details_registered.setVisibility(View.VISIBLE);
                            topHolder.tv_game_details_registered_msg_taskfive.setVisibility(View.GONE);

                            if (accountArray.length() == 1) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.GONE);

                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + "  :  " + accountArray.getJSONObject(0).getString("value"));
                            } else if (accountArray.length() == 2) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.VISIBLE);


                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + "  :  " + accountArray.getJSONObject(0).getString("value"));
                                topHolder.tv_game_details_registered_msg_gamename.setText(accountArray.getJSONObject(1).getString("name") + "  :  " + accountArray.getJSONObject(1).getString("value"));
                            } else if (accountArray.length() == 3) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.VISIBLE);


                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + "  :  " + accountArray.getJSONObject(0).getString("value"));
                                topHolder.tv_game_details_registered_msg_gamename.setText(accountArray.getJSONObject(1).getString("name") + "  :  " + accountArray.getJSONObject(1).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskone.setText(accountArray.getJSONObject(2).getString("name") + "  :  " + accountArray.getJSONObject(2).getString("value"));
                            } else if (accountArray.length() == 4) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.VISIBLE);


                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + "  :  " + accountArray.getJSONObject(0).getString("value"));
                                topHolder.tv_game_details_registered_msg_gamename.setText(accountArray.getJSONObject(1).getString("name") + "  :  " + accountArray.getJSONObject(1).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskone.setText(accountArray.getJSONObject(2).getString("name") + "  :  " + accountArray.getJSONObject(2).getString("value"));
                                topHolder.tv_game_details_registered_msg_tasktwo.setText(accountArray.getJSONObject(3).getString("name") + "  :  " + accountArray.getJSONObject(3).getString("value"));
                            } else if (accountArray.length() == 5) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.VISIBLE);


                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + ":" + accountArray.getJSONObject(0).getString("value"));
                                topHolder.tv_game_details_registered_msg_gamename.setText(accountArray.getJSONObject(1).getString("name") + ":" + accountArray.getJSONObject(1).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskone.setText(accountArray.getJSONObject(2).getString("name") + ":" + accountArray.getJSONObject(2).getString("value"));
                                topHolder.tv_game_details_registered_msg_tasktwo.setText(accountArray.getJSONObject(3).getString("name") + ":" + accountArray.getJSONObject(3).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskthree.setText(accountArray.getJSONObject(4).getString("name") + ":" + accountArray.getJSONObject(4).getString("value"));
                            } else if (accountArray.length() == 6) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_taskfour.setVisibility(View.GONE);
                                topHolder.tv_game_details_registered_msg_taskfive.setVisibility(View.VISIBLE);

                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + ":" + accountArray.getJSONObject(0).getString("value"));
                                topHolder.tv_game_details_registered_msg_gamename.setText(accountArray.getJSONObject(1).getString("name") + ":" + accountArray.getJSONObject(1).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskone.setText(accountArray.getJSONObject(2).getString("name") + ":" + accountArray.getJSONObject(2).getString("value"));
                                topHolder.tv_game_details_registered_msg_tasktwo.setText(accountArray.getJSONObject(3).getString("name") + ":" + accountArray.getJSONObject(3).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskthree.setText(accountArray.getJSONObject(4).getString("name") + ":" + accountArray.getJSONObject(4).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskfive.setText(accountArray.getJSONObject(5).getString("name") + ":" + accountArray.getJSONObject(5).getString("value"));
                            } else if (accountArray.length() == 7) {
                                topHolder.tv_game_details_registered_msg_gameid.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_gamename.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_taskfour.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_msg_taskfive.setVisibility(View.VISIBLE);

                                topHolder.tv_game_details_registered_msg_gameid.setText(accountArray.getJSONObject(0).getString("name") + ":" + accountArray.getJSONObject(0).getString("value"));
                                topHolder.tv_game_details_registered_msg_gamename.setText(accountArray.getJSONObject(1).getString("name") + ":" + accountArray.getJSONObject(1).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskone.setText(accountArray.getJSONObject(2).getString("name") + ":" + accountArray.getJSONObject(2).getString("value"));
                                topHolder.tv_game_details_registered_msg_tasktwo.setText(accountArray.getJSONObject(3).getString("name") + ":" + accountArray.getJSONObject(3).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskthree.setText(accountArray.getJSONObject(4).getString("name") + ":" + accountArray.getJSONObject(4).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskfour.setText(accountArray.getJSONObject(6).getString("name") + ":" + accountArray.getJSONObject(6).getString("value"));
                                topHolder.tv_game_details_registered_msg_taskfive.setText(accountArray.getJSONObject(5).getString("name") + ":" + accountArray.getJSONObject(5).getString("value"));
                            }
                        }

                    } else {
                        topHolder.tv_game_details_registered_title.setText("无法参与");
                        topHolder.tv_game_details_registered_cant_msg.setVisibility(View.VISIBLE);
                        topHolder.ll_game_details_registered.setVisibility(View.GONE);

                        topHolder.tv_game_details_registered_cant_msg.setText("无法参与该游戏！您已经在本平台参与过此游戏或者其他平台参与过此游戏。");

                        //tv_gamedetails_start_game.setText("无法参与");
                        tv_gamedetails_start_game.setText(gameListData.getJSONObject("button").getString("title"));
                        ll_gamedetails_start_game.setEnabled(false);

                        String limitgame = SessionSingleton.getInstance().mysp.getString("limitgame", "") + gameListData.getJSONObject("base_info").getString("name");
                        SessionSingleton.getInstance().mysp.edit().putString("limitgame", limitgame).commit();

                    }

                    if (array.length() == 1) {
                        ll_game_details_item_type_one.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_two.setVisibility(View.GONE);
                        ll_game_details_item_type_three.setVisibility(View.GONE);
                        ll_game_details_item_type_four.setVisibility(View.GONE);


                        tv_game_details_item_type_one.setText(array.getJSONObject("play").getString("name"));


                    } else if (array.length() == 2) {
                        ll_game_details_item_type_one.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_two.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_three.setVisibility(View.GONE);
                        ll_game_details_item_type_four.setVisibility(View.GONE);

                        tv_game_details_item_type_one.setText(array.getJSONObject("play").getString("name"));

                        if (array.has("charge")) {
                            tv_game_details_item_type_two.setText(array.getJSONObject("charge").getString("name"));
                        } else if (array.has("race")) {
                            tv_game_details_item_type_two.setText(array.getJSONObject("race").getString("name"));
                        }

                    } else if (array.length() == 3) {
                        ll_game_details_item_type_one.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_two.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_three.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_four.setVisibility(View.GONE);

                        tv_game_details_item_type_one.setText(array.getJSONObject("play").getString("name"));
                        tv_game_details_item_type_two.setText(array.getJSONObject("charge").getString("name"));
                        tv_game_details_item_type_three.setText(array.getJSONObject("race").getString("name"));
                    }


                    if (isFirstShow == 0) {
                        isFirstShow = isFirstShow + 1;
                        topHolder.tv_game_details_shiwanwanfei.setText(array.getJSONObject("play").getString("tip"));
                    }

                    if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                        topHolder.tv_game_details_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                        topHolder.ll_game_details_title_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());


                        if (SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon() == 0) {
                            topHolder.iv_game_details_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                        } else {
                            topHolder.iv_game_details_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                        }

                    }

                    topHolder.tv_game_details_title.setText(gameListData.getJSONObject("base_info").getString("name"));
                    topHolder.tv_game_details_name.setText(gameListData.getJSONObject("base_info").getString("app_name"));
                    if (gameListData.getJSONObject("base_info").getString("app_size").equals("")) {
                        topHolder.tv_game_details_size.setVisibility(View.GONE);
                    } else {
                        topHolder.tv_game_details_size.setText(gameListData.getJSONObject("base_info").getString("app_size") + "MB");
                    }
                    topHolder.tv_game_details_money.setText(gameListData.getJSONObject("base_info").getString("max_prize") + "元");

                    Long stoptime = gameListData.getJSONObject("base_info").getLong("end_time") * 1000;
                    Long starttime = System.currentTimeMillis();

                    StopTime = stoptime;
                    if (stoptime > starttime) {
                        Long time = stoptime - starttime;
                        String shengyutime = gameListData.getJSONObject("base_info").getString("surplus_day");

                        if (shengyutime.length() == 1) {
                            topHolder.tv_game_details_time_one.setVisibility(View.GONE);
                            topHolder.tv_game_details_time_two.setVisibility(View.GONE);
                            topHolder.tv_game_details_time_three.setText("0");
                            topHolder.tv_game_details_time_four.setText(shengyutime.substring(0, 1));
                        } else if (shengyutime.length() == 2) {
                            topHolder.tv_game_details_time_one.setVisibility(View.GONE);
                            topHolder.tv_game_details_time_two.setVisibility(View.GONE);
                            topHolder.tv_game_details_time_three.setText(shengyutime.substring(0, 1));
                            topHolder.tv_game_details_time_four.setText(shengyutime.substring(1, 2));
                        } else if (shengyutime.length() == 3) {
                            topHolder.tv_game_details_time_one.setVisibility(View.GONE);
                            topHolder.tv_game_details_time_two.setText(shengyutime.substring(0, 1));
                            topHolder.tv_game_details_time_three.setText(shengyutime.substring(1, 2));
                            topHolder.tv_game_details_time_four.setText(shengyutime.substring(2, 3));
                        } else if (shengyutime.length() == 4) {
                            topHolder.tv_game_details_time_one.setText(shengyutime.substring(0, 1));
                            topHolder.tv_game_details_time_two.setText(shengyutime.substring(1, 2));
                            topHolder.tv_game_details_time_three.setText(shengyutime.substring(2, 3));
                            topHolder.tv_game_details_time_four.setText(shengyutime.substring(3, 4));
                        }

                    } else {
                        topHolder.tv_game_details_time_one.setVisibility(View.GONE);
                        topHolder.tv_game_details_time_two.setVisibility(View.GONE);
                        topHolder.tv_game_details_time_three.setVisibility(View.GONE);
                        topHolder.tv_game_details_time_four.setText("0");
                    }


                    //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                    Bitmap bmp = imageLoader.loadBitmap(topHolder.iv_game_details_image, gameListData.getJSONObject("base_info").getString("images"));
                    if (bmp == null) {
                        topHolder.iv_game_details_image.setImageResource(R.drawable.ic_load_iname);
                    } else {
                        topHolder.iv_game_details_image.setImageBitmap(bmp);
                    }


                    topHolder.iv_game_details_registered_refresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mLoading.show();
                            getJuXiangWanGameDetails(juxiangwanId);
                        }
                    });

                    topHolder.tv_game_details_account_msg_open_down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (isOpenRules == 0) {
                                isOpenRules = 1;
                                topHolder.iv_game_details_account_msg_open_down.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_up_green));
                                topHolder.tv_game_details_account_msg_open_down.setText("点击收起");
                                topHolder.ll_game_details_account_msg_rules.setVisibility(View.VISIBLE);
                            } else {
                                isOpenRules = 0;
                                topHolder.iv_game_details_account_msg_open_down.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_down_green));
                                topHolder.tv_game_details_account_msg_open_down.setText("点击展开");
                                topHolder.ll_game_details_account_msg_rules.setVisibility(View.GONE);
                            }
                        }
                    });

                    ll_game_details_item_type_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            tv_game_details_item_type_one_zhishiqi.setVisibility(View.VISIBLE);
                            tv_game_details_item_type_two_zhishiqi.setVisibility(View.GONE);
                            tv_game_details_item_type_three_zhishiqi.setVisibility(View.GONE);
                            tv_game_details_item_type_four_zhishiqi.setVisibility(View.GONE);

                            tv_game_details_item_type_one.setTextSize(17);
                            tv_game_details_item_type_two.setTextSize(14);
                            tv_game_details_item_type_three.setTextSize(14);
                            tv_game_details_item_type_four.setTextSize(14);

                            tv_game_details_item_type_one.setTextColor(getResources().getColor(R.color.yunbu_textblack));
                            tv_game_details_item_type_two.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            tv_game_details_item_type_three.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            tv_game_details_item_type_four.setTextColor(getResources().getColor(R.color.yunbu_textgray));


                            ListType = "1";

                            try {

                                taskArray = gameListData.getJSONObject("task_detail").getJSONObject("play").getJSONArray("list");
                                topHolder.tv_game_details_shiwanwanfei.setText(gameListData.getJSONObject("task_detail").getJSONObject("play").getString("tip"));
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    ll_game_details_item_type_two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tv_game_details_item_type_one_zhishiqi.setVisibility(View.GONE);
                            tv_game_details_item_type_two_zhishiqi.setVisibility(View.VISIBLE);
                            tv_game_details_item_type_three_zhishiqi.setVisibility(View.GONE);
                            tv_game_details_item_type_four_zhishiqi.setVisibility(View.GONE);

                            tv_game_details_item_type_one.setTextSize(14);
                            tv_game_details_item_type_two.setTextSize(17);
                            tv_game_details_item_type_three.setTextSize(14);
                            tv_game_details_item_type_four.setTextSize(14);

                            tv_game_details_item_type_one.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            tv_game_details_item_type_two.setTextColor(getResources().getColor(R.color.yunbu_textblack));
                            tv_game_details_item_type_three.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            tv_game_details_item_type_four.setTextColor(getResources().getColor(R.color.yunbu_textgray));


                            try {
                                if (gameListData.getJSONObject("task_detail").has("charge")) {
                                    ListType = "2";
                                    taskArray = gameListData.getJSONObject("task_detail").getJSONObject("charge").getJSONArray("list");
                                    topHolder.tv_game_details_shiwanwanfei.setText(gameListData.getJSONObject("task_detail").getJSONObject("charge").getString("tip"));
                                } else if (gameListData.getJSONObject("task_detail").has("race")) {
                                    ListType = "3";
                                    taskArray = gameListData.getJSONObject("task_detail").getJSONObject("race").getJSONArray("list");
                                    topHolder.tv_game_details_shiwanwanfei.setText(gameListData.getJSONObject("task_detail").getJSONObject("race").getString("tip"));
                                }


                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    ll_game_details_item_type_three.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tv_game_details_item_type_one_zhishiqi.setVisibility(View.GONE);
                            tv_game_details_item_type_two_zhishiqi.setVisibility(View.GONE);
                            tv_game_details_item_type_three_zhishiqi.setVisibility(View.VISIBLE);
                            tv_game_details_item_type_four_zhishiqi.setVisibility(View.GONE);

                            tv_game_details_item_type_one.setTextSize(14);
                            tv_game_details_item_type_two.setTextSize(14);
                            tv_game_details_item_type_three.setTextSize(17);
                            tv_game_details_item_type_four.setTextSize(14);

                            tv_game_details_item_type_one.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            tv_game_details_item_type_two.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            tv_game_details_item_type_three.setTextColor(getResources().getColor(R.color.yunbu_textblack));
                            tv_game_details_item_type_four.setTextColor(getResources().getColor(R.color.yunbu_textgray));

                            ListType = "3";

                            try {
                                taskArray = gameListData.getJSONObject("task_detail").getJSONObject("race").getJSONArray("list");
                                topHolder.tv_game_details_shiwanwanfei.setText(gameListData.getJSONObject("task_detail").getJSONObject("race").getString("tip"));


                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    topHolder.iv_game_details_back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                if (ListType.equals("3")) {
                    viewTypeTwoHolder = new ViewTypeTwoHolder();
                    convertView = inflater.inflate(R.layout.item_yun_bu_game_details_zhankai, null);
                    viewTypeTwoHolder.tv_item_game_details_fuli_num = (TextView) convertView.findViewById(R.id.tv_item_game_details_fuli_num);
                    viewTypeTwoHolder.tv_item_game_details_fuli_msg = (TextView) convertView.findViewById(R.id.tv_item_game_details_fuli_msg);
                    viewTypeTwoHolder.tv_item_game_details_fuli_open = (TextView) convertView.findViewById(R.id.tv_item_game_details_fuli_open);
                    viewTypeTwoHolder.tv_item_game_details_fuli_image = (ImageView) convertView.findViewById(R.id.tv_item_game_details_fuli_image);
                    viewTypeTwoHolder.ll_item_game_details_fuli_list = (LinearLayout) convertView.findViewById(R.id.ll_item_game_details_fuli_list);
                    viewTypeTwoHolder.lv_item_game_details_fuli_list = (ListView) convertView.findViewById(R.id.lv_item_game_details_fuli_list);
                    viewTypeTwoHolder.ll_item_game_details_fuli = (LinearLayout) convertView.findViewById(R.id.ll_item_game_details_fuli);

                    try {
                        final JSONObject single = taskArray.getJSONObject(position - 1);

                        viewTypeTwoHolder.tv_item_game_details_fuli_msg.setText(single.getString("name"));
                        //viewTypeTwoHolder.tv_item_game_details_fuli_num.setText(position + "");
                        viewTypeTwoHolder.tv_item_game_details_fuli_num.setText("" + position);


                        viewTypeTwoHolder.tv_item_game_details_fuli_open.setText("展开");
                        viewTypeTwoHolder.tv_item_game_details_fuli_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_down_green));


                        viewTypeTwoHolder.ll_item_game_details_fuli.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (isOpenChecked == 1) {
                                    isOpenChecked = 2;
                                    viewTypeTwoHolder.tv_item_game_details_fuli_open.setText("收起");
                                    viewTypeTwoHolder.tv_item_game_details_fuli_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_up_green));

                                    viewTypeTwoHolder.ll_item_game_details_fuli_list.setVisibility(View.VISIBLE);
                                    try {
                                        TypeThreeArray = single.getJSONArray("list");
                                        typeThreeAdapter = new TypeThreeAdapter(context);
                                        viewTypeTwoHolder.lv_item_game_details_fuli_list.setAdapter(typeThreeAdapter);
                                        Utils.setListViewHeightBasedOnChildren(viewTypeTwoHolder.lv_item_game_details_fuli_list);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    isOpenChecked = 1;
                                    viewTypeTwoHolder.tv_item_game_details_fuli_open.setText("展开");
                                    viewTypeTwoHolder.tv_item_game_details_fuli_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_down_green));
                                    viewTypeTwoHolder.ll_item_game_details_fuli_list.setVisibility(View.GONE);

                                }


                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.item_yun_bu_game_details_item, null);
                    holder.tv_item_game_details_type = (TextView) convertView.findViewById(R.id.tv_item_game_details_type);
                    holder.tv_item_game_details_msg = (TextView) convertView.findViewById(R.id.tv_item_game_details_msg);
                    holder.tv_item_game_details_money = (TextView) convertView.findViewById(R.id.tv_item_game_details_money);
                    holder.tv_item_game_details_num = (TextView) convertView.findViewById(R.id.tv_item_game_details_num);

                    try {
                        JSONObject single = null;
                        if (ListType.equals("1")) {
                            if (ShowFirst.equals("none")) {
                                single = taskArray.getJSONObject(position);
                            } else {
                                single = taskArray.getJSONObject(position - 1);
                            }
                        } else {
                            single = taskArray.getJSONObject(position - 1);
                        }

                        holder.tv_item_game_details_msg.setText(single.getString("name"));

                        //holder.tv_item_game_details_num.setText(position + "");
                        holder.tv_item_game_details_num.setText("" + position);


                        //  	状态 1已领奖 3未达标 5待发奖
                        if (single.getInt("status") == 3) {
                            holder.tv_item_game_details_type.setText("待完成");
                            holder.tv_item_game_details_type.setTextColor(getResources().getColor(R.color.yunbu_textchecked));
                            holder.tv_item_game_details_msg.setTextColor(getResources().getColor(R.color.yunbu_textblack));
                            holder.tv_item_game_details_money.setTextColor(getResources().getColor(R.color.yunbu_textchecked));

                            // holder.tv_item_game_details_type.setBackground(getResources().getDrawable(R.color.transparent));

                        } else if (single.getInt("status") == 5) {
                            holder.tv_item_game_details_type.setText("可领取");
                            holder.tv_item_game_details_type.setTextColor(getResources().getColor(R.color.yunbu_textchecked));
                            holder.tv_item_game_details_msg.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            holder.tv_item_game_details_money.setTextColor(getResources().getColor(R.color.yunbu_textchecked));


                        } else if (single.getInt("status") == 1) {
                            holder.tv_item_game_details_type.setText("已领取");
                            holder.tv_item_game_details_type.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            holder.tv_item_game_details_msg.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            holder.tv_item_game_details_money.setTextColor(getResources().getColor(R.color.yunbu_textgray));

                            // holder.tv_item_game_details_type.setBackground(getResources().getDrawable(R.color.transparent));
                        }


                        String m1 = single.getString("deal_prize").replace(",", "");
                        String m2 = single.getString("task_prize").replace(",", "");

                        double getmoney = Double.valueOf(m1) + Double.valueOf(m2);

                        double finalmoney = 0;
                        if (ListType.equals("1")) {
                            double money = getmoney - (getmoney * SessionSingleton.getInstance().yunbuGameMoneyScale);
                            finalmoney = money - (money * SessionSingleton.getInstance().moneyScale);

                            BigDecimal sharemal = new BigDecimal(finalmoney);
                            finalmoney = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        } else if (ListType.equals("2")) {
                            double money = getmoney - (getmoney * SessionSingleton.getInstance().yunbuGameChangeScale);
                            finalmoney = money - (money * SessionSingleton.getInstance().moneyScaleGameRecharge);

                            BigDecimal sharemal = new BigDecimal(finalmoney);
                            finalmoney = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        }
                        //holder.tv_item_game_details_money.setText(Utils.getDoubleString(Double.valueOf(finalmoney)) + "元");
                        holder.tv_item_game_details_money.setText(finalmoney + SessionSingleton.getInstance().AccountSingle.getString("moneyUnit"));


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }


            return convertView;
        }

        class ViewTopHolder {
            ImageView iv_game_details_back, iv_game_details_image, iv_game_details_registered_refresh;
            TextView tv_game_details_title, tv_game_details_name, tv_game_details_size, tv_game_details_type, tv_game_details_money;
            TextView tv_game_details_time_one, tv_game_details_time_two, tv_game_details_time_three, tv_game_details_time_four;

            LinearLayout ll_game_details_title_background;

            TextView tv_game_details_registered_title, tv_game_details_shiwanwanfei;
            LinearLayout ll_game_details_registered;
            TextView tv_game_details_registered_cant_msg;
            TextView tv_game_details_registered_msg_gameid, tv_game_details_registered_msg_gamename, tv_game_details_registered_msg_taskone,
                    tv_game_details_registered_msg_tasktwo, tv_game_details_registered_msg_taskthree, tv_game_details_registered_msg_taskfour, tv_game_details_registered_msg_taskfive;

            ImageView iv_game_details_account_msg_open_down;
            TextView tv_game_details_account_msg_open_down;
            LinearLayout ll_game_details_account_msg_rules;

            TextView tv_game_details_rules_qq;
        }

        class ViewTypeTwoHolder {
            TextView tv_item_game_details_fuli_msg, tv_item_game_details_fuli_open, tv_item_game_details_fuli_num;
            ImageView tv_item_game_details_fuli_image;
            LinearLayout ll_item_game_details_fuli_list, ll_item_game_details_fuli;
            ListView lv_item_game_details_fuli_list;
        }

        class ViewHolder {
            TextView tv_item_game_details_type, tv_item_game_details_msg, tv_item_game_details_money, tv_item_game_details_num;
        }
    }

    public class TypeThreeAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public TypeThreeAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return TypeThreeArray.length();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_yun_bu_game_details_zhankai_item, null);
                holder.tv_item_game_details_fuli_item_num = convertView.findViewById(R.id.tv_item_game_details_fuli_item_num);
                holder.tv_item_game_details_fuli_item_id = convertView.findViewById(R.id.tv_item_game_details_fuli_item_id);
                holder.tv_item_game_details_fuli_item_money = convertView.findViewById(R.id.tv_item_game_details_fuli_item_money);
                holder.tv_item_game_details_fuli_item_image = convertView.findViewById(R.id.tv_item_game_details_fuli_item_image);
                holder.tv_item_game_details_fuli_item_unit = convertView.findViewById(R.id.tv_item_game_details_fuli_item_unit);

                holder.ll_item_game_details_fuli_item = convertView.findViewById(R.id.ll_item_game_details_fuli_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                JSONObject object = TypeThreeArray.getJSONObject(position);

                if (object.has("rolename")) {
                    holder.tv_item_game_details_fuli_item_id.setText(object.getString("rolename"));
                } else {
                    holder.tv_item_game_details_fuli_item_id.setText("虚位以待");
                }


                String m1 = object.getString("deal_prize").replace(",", "");
                String m2 = object.getString("task_prize").replace(",", "");

                double money = Double.valueOf(m1) + Double.valueOf(m2);

                double finalmoney = money - (money * SessionSingleton.getInstance().yunbuGameGradeSerailMoneyScale);

                BigDecimal sharemal = new BigDecimal(finalmoney);
                finalmoney = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                holder.tv_item_game_details_fuli_item_money.setText(finalmoney + "");


                if (position + 1 == 1) {
                    holder.tv_item_game_details_fuli_item_num.setVisibility(View.GONE);
                    holder.tv_item_game_details_fuli_item_image.setVisibility(View.VISIBLE);
                    holder.tv_item_game_details_fuli_item_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_details_top3));

                } else if (position + 1 == 2) {
                    holder.tv_item_game_details_fuli_item_num.setVisibility(View.GONE);
                    holder.tv_item_game_details_fuli_item_image.setVisibility(View.VISIBLE);
                    holder.tv_item_game_details_fuli_item_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_details_top3));
                } else if (position + 1 == 3) {
                    holder.tv_item_game_details_fuli_item_num.setVisibility(View.GONE);
                    holder.tv_item_game_details_fuli_item_image.setVisibility(View.VISIBLE);
                    holder.tv_item_game_details_fuli_item_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_details_top3));
                } else {
                    holder.tv_item_game_details_fuli_item_num.setVisibility(View.VISIBLE);
                    holder.tv_item_game_details_fuli_item_image.setVisibility(View.GONE);
                    holder.tv_item_game_details_fuli_item_num.setText(object.getString("rank"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return convertView;
        }

        public class ViewHolder {
            TextView tv_item_game_details_fuli_item_num, tv_item_game_details_fuli_item_id, tv_item_game_details_fuli_item_money, tv_item_game_details_fuli_item_unit;
            ImageView tv_item_game_details_fuli_item_image;
            LinearLayout ll_item_game_details_fuli_item;
        }

    }


    public void bindService(String apkUrl) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_URL, apkUrl);
        isBindService = context.bindService(intent, conn, BIND_AUTO_CREATE);
    }

    /**
     * 删除上次更新存储在本地的apk
     */
    private void removeOldApk(String s) {
        //获取老ＡＰＫ的存储路径
        File fileName = new File(s);

        if (fileName != null && fileName.exists() && fileName.isFile()) {
            fileName.delete();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (installindex != 0) {
            installindex = installindex + 1;
            removeOldApk(fileUrl);
        }*/
        removeOldApk(fileUrl);

        ll_gamedetails_start_game.setVisibility(View.VISIBLE);
        ll_gamedetails_load_game.setVisibility(View.GONE);


        isRefresh = isRefresh + 1;
        if (isRefresh >= 2) {
            getJuXiangWanGameDetails(juxiangwanId);
        }
    }

    //提示
    private void showToastTISHI() {

        builder = new AlertDialog.Builder(context);//创建对话框

        inflater = getLayoutInflater();
        layout = inflater.inflate(R.layout.pop_yun_bu_game_detials_tishi, null);//获取自定义布局
        builder.setView(layout);//设置对话框的布局
        dialog = builder.create();//生成最终的对话框

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (Utils.checkAppInstalled(SessionSingleton.getInstance().ctx, packgeName) == true) {

                    tv_gamedetails_start_game.setText("开始任务");
                    ll_gamedetails_start_game.setVisibility(View.VISIBLE);
                    ll_gamedetails_load_game.setVisibility(View.GONE);

                    PackageManager packageManager = getPackageManager();
                    Intent intent = new Intent();
                    intent = packageManager.getLaunchIntentForPackage(packgeName);
                    if (intent == null) {
                        Toast.makeText(context, "未安装", Toast.LENGTH_LONG).show();
                    } else {
                        startActivity(intent);
                    }
                } else {
                    tv_gamedetails_start_game.setText("开始任务");
                    ll_gamedetails_start_game.setVisibility(View.GONE);
                    ll_gamedetails_load_game.setVisibility(View.VISIBLE);

                    removeOldApk(fileUrl);
                    //initDownload(DownUrl);
                    bindService(DownUrl);
                }
            }
        });


    }

    //提示
    private void showToast(final int status, String msg) {

        LayoutInflater la = LayoutInflater.from(context);
        View contentView = la.inflate(R.layout.pop_yun_bu_yun_bu_game_details_toast_show, null);//自定义布局

        TextView tv_toast_msg = contentView.findViewById(R.id.tv_game_details_toast_msg);
        TextView tv_toast_close = contentView.findViewById(R.id.tv_game_details_toast_close);
        TextView tv_game_details_toast_msg_title = contentView.findViewById(R.id.tv_game_details_toast_msg_title);


        if (status == 0) {
            tv_toast_msg.setText("游戏凭证：" + account);
        } else {
            tv_toast_msg.setText(msg);
            tv_game_details_toast_msg_title.setVisibility(View.GONE);
            tv_toast_close.setText("退出");
        }


        tv_toast_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (status == 0) {
                    tosatPopuo.dismiss();

                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", account);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);


                    openQQChat(context, kefuQQ);
                } else {
                    tosatPopuo.dismiss();
                    finish();
                }


            }
        });


        tosatPopuo = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


        //设置PopupWindow的焦点
        tosatPopuo.setFocusable(true);
        tosatPopuo.setClippingEnabled(false);
        //点击PopupWindow之外的地方PopupWindow会消失
        tosatPopuo.setOutsideTouchable(true);
        //showAtLocation(View parent, int gravity, int x, int y)：相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
        tosatPopuo.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        tosatPopuo.update();
    }

    /**
     * 打开指定的QQ聊天页面
     *
     * @param context 上下文
     * @param QQ      QQ号码
     */
    public static boolean openQQChat(Context context, String QQ) {
        try {
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + QQ;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
