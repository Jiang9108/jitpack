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
import android.text.Html;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


public class YunBuGameDetailsXiQuActivity extends AppCompatActivity {
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
    private JSONObject gameTitleData;

    private GameDetailsAdapter adapter;
    private TypeThreeAdapter typeThreeAdapter;
    private String DownUrl, account, keycode, deviceid, msaoaid, androidosv, packgeName, fileUrl = "/storage/emulated/0/game.apk";
    private String adid;
    private int limit, isOpenChecked = 1, isOpenRules = 0;
    private String ListType = "1";
    private JSONArray TypeThreeArray;
    private String ShowFirst;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View layout;

    int isRefresh = 0;

    long StopTime = 0L;

    String kefuQQ;
    private double controlMoneyScale = 0;


    private boolean isBindService;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
            DownloadService downloadService = binder.getService();

            //???????????????????????????
            downloadService.setOnProgressListener(new DownloadService.OnProgressListener() {
                @Override
                public void onProgress(float fraction) {
                    Log.i("", "???????????????" + fraction);
                    //int num=(int)(fraction*100);

                    int progress = Math.round(fraction);
                    //float progress=fraction/100;

                    //pb_gamedetails.setWaveLevelRatio(progress);
                    pb_gamedetails.setProgress(progress);

                    //?????????????????????????????????????????????????????????????????????????????????
                    if (fraction == DownloadService.UNBIND_SERVICE && isBindService) {
                        context.unbindService(conn);
                        isBindService = false;
                        Utils.showToast(context, "???????????????");

                        tv_gamedetails_start_game.setText("????????????");
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
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
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

        adid = intent.getStringExtra("ADID");
        ShowFirst = intent.getStringExtra("ShowFirst");
        kefuQQ = intent.getStringExtra("KeFuQQ");


        mLoading = Utils.createLoadingDialog(context, "????????????......");
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
                showToast(0,"");
            }
        });
        ll_gamedetails_gonglue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");//???????????????????????????
                intent.setData(Uri.parse("https://www.baidu.com"));//??????
                //?????????????????????
                startActivity(intent);
            }
        });
        ll_gamedetails_start_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long StartTime = System.currentTimeMillis();

                //5???????????????
                if (StopTime - StartTime > 300000) {
                    showToastTISHI();
                } else {
                    Utils.showToast(context, "???????????????????????????????????????");
                    try {
                        String limitgame =     SessionSingleton.getInstance().mysp .getString("limitgame", "") +gameTitleData.getJSONObject("baseInfo").getString("adName");
                        SessionSingleton.getInstance().mysp .edit().putString("limitgame", limitgame).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
    }

    private void initview() {
        gameTitleData = new JSONObject();
        taskArray = new JSONArray();
        TypeThreeArray = new JSONArray();


        xlv_gamedetails = (XListView) findViewById(R.id.xlv_gamedetails);
        tv_gamedetails_start_game = (TextView) findViewById(R.id.tv_gamedetails_start_game);

        ll_gamedetails_qq = (LinearLayout) findViewById(R.id.ll_gamedetails_qq);
        ll_gamedetails_gonglue = (LinearLayout) findViewById(R.id.ll_gamedetails_gonglue);

        ll_gamedetails_start_game = (LinearLayout) findViewById(R.id.ll_gamedetails_start_game);
        ll_gamedetails_load_game = findViewById(R.id.rl_gamedetails_load_game);

        pb_gamedetails = (ProgressBar) findViewById(R.id.yunbu_pb_gamedetails);


        try {
            deviceid = SessionSingleton.getInstance().AccountSingle.getString("deviceNumber");
            account = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");
            //msaoaid = SessionSingleton.getInstance().AccountSingle.getString("oaId");
            androidosv = String.valueOf(Build.VERSION.SDK_INT);



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                msaoaid = SessionSingleton.getInstance().AccountSingle.getString("oaId");
            } else {
                msaoaid = "";
            }


            controlMoneyScale = SessionSingleton.getInstance().AccountSingle.getDouble("moneyScale");

            keycode = Utils.getMD5("2" + deviceid + msaoaid + androidosv + SessionSingleton.getInstance().XiQuSingle.getString("key") + account + adid + SessionSingleton.getInstance().XiQuSingle.getString("secret"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ListType = "1";


        XiQugame(adid);


        xlv_gamedetails.setPullRefreshEnable(true);
        xlv_gamedetails.setPullLoadEnable(false);
        adapter = new GameDetailsAdapter(context);
        xlv_gamedetails.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                mLoading.show();
                XiQugame(adid);
                xlv_gamedetails.stopRefresh();
            }

            @Override
            public void onLoadMore() {
                xlv_gamedetails.stopLoadMore();
            }

        });

        adapter = new GameDetailsAdapter(context);
        xlv_gamedetails.setAdapter(adapter);


    }


    public class GameDetailsAdapter extends BaseAdapter {
        private AsyncImageLoader imageLoader;//????????????
        private LayoutInflater inflater;


        public GameDetailsAdapter(Context context) {
            //inflater = LayoutInflater.from(context);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            MemoryCache mcache = new MemoryCache();//????????????
            String paht = getApplicationContext().getFilesDir().getAbsolutePath();
            File cacheDir = new File(paht, "yunbucache");//???????????????
            FileCache fcache = new FileCache(context, cacheDir, "yunbuimage");//????????????
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final ViewTopHolder topHolder;
            final ViewTypeTwoHolder viewTypeTwoHolder;

            int type = getItemViewType(position);
            if (type == 0) {
                if (convertView == null) {
                    //929373
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

                    topHolder.tv_game_details_registered_msg = convertView.findViewById(R.id.tv_game_details_registered_msg);
                    topHolder.ll_game_details_registered = convertView.findViewById(R.id.ll_game_details_registered);

                    topHolder.tv_game_details_registered_title = convertView.findViewById(R.id.tv_game_details_registered_title);
                    topHolder.tv_game_details_registered_cant_msg = convertView.findViewById(R.id.tv_game_details_registered_cant_msg);
                    topHolder.tv_game_details_registered_tishi = convertView.findViewById(R.id.tv_game_details_registered_tishi);

                    topHolder.tv_game_details_account_msg_open_down = convertView.findViewById(R.id.tv_game_details_account_msg_open_down);
                    topHolder.iv_game_details_account_msg_open_down = convertView.findViewById(R.id.iv_game_details_account_msg_open_down);
                    topHolder.ll_game_details_account_msg_rules = convertView.findViewById(R.id.ll_game_details_account_msg_rules);

                    topHolder.tv_game_details_registered_msg_gameid = convertView.findViewById(R.id.tv_game_details_registered_msg_gameid);
                    topHolder.tv_game_details_registered_msg_gamename = convertView.findViewById(R.id.tv_game_details_registered_msg_gamename);
                    topHolder.tv_game_details_registered_msg_taskone = convertView.findViewById(R.id.tv_game_details_registered_msg_taskone);
                    topHolder.tv_game_details_registered_msg_tasktwo = convertView.findViewById(R.id.tv_game_details_registered_msg_tasktwo);
                    topHolder.tv_game_details_registered_msg_taskthree = convertView.findViewById(R.id.tv_game_details_registered_msg_taskthree);
                    topHolder.tv_game_details_registered_msg_taskfour = convertView.findViewById(R.id.tv_game_details_registered_msg_taskfour);
                    topHolder.tv_game_details_registered_msg_taskfive = convertView.findViewById(R.id.tv_game_details_registered_msg_taskfive);

                    topHolder.iv_game_details_registered_refresh = convertView.findViewById(R.id.iv_game_details_registered_refresh);

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

                    topHolder.tv_game_details_rules_qq.setText("3?????????-??????????????????????????????????????????????????????QQ???" + kefuQQ + "???????????????????????????9???00-18???00???");

                    JSONObject single = gameTitleData.getJSONObject("baseInfo");
                    JSONArray array = gameTitleData.getJSONArray("awardName");
                    String bangdan = gameTitleData.getString("activityList");

                    limit = single.getInt("limit");
                    packgeName = single.getString("pageName");

                    if (limit == 1) {
                        topHolder.tv_game_details_registered_title.setText("????????????");
                        topHolder.ll_game_details_registered.setVisibility(View.GONE);

                        topHolder.tv_game_details_registered_cant_msg.setVisibility(View.VISIBLE);
                        topHolder.tv_game_details_registered_cant_msg.setText("??????????????????????????????????????????????????????????????????????????????????????????????????????");

                        tv_gamedetails_start_game.setText("????????????");
                        ll_gamedetails_start_game.setEnabled(false);

                        String limitgame =     SessionSingleton.getInstance().mysp.getString("limitgame", "") +gameTitleData.getJSONObject("baseInfo").getString("adName");
                        SessionSingleton.getInstance().mysp.edit().putString("limitgame", limitgame).commit();
                    } else {
                        tv_gamedetails_start_game.setText("????????????");

                        if (single.getString("appBind").equals("false")) {
                            topHolder.tv_game_details_registered_title.setText("????????????");
                            topHolder.ll_game_details_registered.setVisibility(View.GONE);
                            topHolder.tv_game_details_registered_cant_msg.setVisibility(View.VISIBLE);

                        } else {
                            if (single.getString("appReg").equals("false")) {
                                topHolder.tv_game_details_registered_title.setText("????????????");
                                topHolder.ll_game_details_registered.setVisibility(View.GONE);

                                topHolder.tv_game_details_registered_msg.setVisibility(View.VISIBLE);
                                topHolder.tv_game_details_registered_tishi.setVisibility(View.GONE);

                                topHolder.tv_game_details_registered_cant_msg.setVisibility(View.GONE);

                            } else {
                                topHolder.tv_game_details_registered_title.setText("????????????");
                                topHolder.ll_game_details_registered.setVisibility(View.GONE);
                                topHolder.tv_game_details_registered_cant_msg.setVisibility(View.GONE);
                                topHolder.tv_game_details_registered_tishi.setVisibility(View.GONE);
                                topHolder.tv_game_details_registered_msg.setVisibility(View.VISIBLE);

                            }
                        }
                    }


                    if (array.length() == 1) {
                        ll_game_details_item_type_one.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_two.setVisibility(View.GONE);
                        ll_game_details_item_type_three.setVisibility(View.GONE);
                        ll_game_details_item_type_four.setVisibility(View.GONE);

                        tv_game_details_item_type_one.setText(array.getString(0));
                    } else if (array.length() == 2) {
                        ll_game_details_item_type_one.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_two.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_three.setVisibility(View.GONE);
                        ll_game_details_item_type_four.setVisibility(View.GONE);

                        tv_game_details_item_type_one.setText(array.getString(0));
                        tv_game_details_item_type_two.setText(array.getString(1));
                    } else if (array.length() == 3) {
                        ll_game_details_item_type_one.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_two.setVisibility(View.VISIBLE);
                        ll_game_details_item_type_three.setVisibility(View.GONE);
                        ll_game_details_item_type_four.setVisibility(View.VISIBLE);

                        tv_game_details_item_type_one.setText(array.getString(0));
                        tv_game_details_item_type_two.setText(array.getString(1));
                        tv_game_details_item_type_four.setText(array.getString(2));
                    }


                    if (bangdan.equals("")) {
                        ll_game_details_item_type_three.setVisibility(View.GONE);
                    } else {
                        ll_game_details_item_type_three.setVisibility(View.VISIBLE);
                        tv_game_details_item_type_three.setText("????????????");
                    }



                    if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                        topHolder.tv_game_details_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                        topHolder.ll_game_details_title_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());


                        if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                            topHolder.iv_game_details_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                        }else{
                            topHolder.iv_game_details_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                        }
                    }

                    topHolder.tv_game_details_title.setText(single.getString("adName"));
                    topHolder.tv_game_details_name.setText(single.getString("adName"));
                    topHolder.tv_game_details_size.setText(single.getString("appSize"));
                    topHolder.tv_game_details_money.setText(single.getString("appAMoney"));

                    String stoptime = single.getString("stopTime");
                    String starttime = single.getString("nowDate");
                    Long start = Utils.data2(starttime, "yyyy-MM-dd HH:mm:ss");
                    Long stop = Utils.data2(stoptime, "yyyy-MM-dd HH:mm:ss");
                    StopTime = stop;
                    if (stop > start) {

                        Long time = stop - start;
                        String shengyutime = Utils.data1(time, "dd");

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

                    //if (single.getString("appReg").equals("true")) {
                        CharSequence charSequence = Html.fromHtml(single.getString("appShowMsg"));
                        topHolder.tv_game_details_registered_msg.setText(charSequence);

                   // }
                 /*   else {
                        topHolder.tv_game_details_no_registered_shuaxin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mLoading.show();
                                XiQugame(adid);
                            }
                        });
                    }*/

                    //????????????????????????????????????????????????????????????????????????????????????
                    Bitmap bmp = imageLoader.loadBitmap(topHolder.iv_game_details_image, single.getString("imgUrl"));
                    if (bmp == null) {
                        topHolder.iv_game_details_image.setImageResource(R.drawable.ic_load_iname);
                    } else {
                        topHolder.iv_game_details_image.setImageBitmap(bmp);
                    }

                    topHolder.iv_game_details_registered_refresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mLoading.show();
                            XiQugame(adid);
                        }
                    });

                    topHolder.tv_game_details_account_msg_open_down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (isOpenRules == 0) {
                                isOpenRules = 1;
                                topHolder.iv_game_details_account_msg_open_down.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_up_green));
                                topHolder.tv_game_details_account_msg_open_down.setText("????????????");
                                topHolder.ll_game_details_account_msg_rules.setVisibility(View.VISIBLE);
                            } else {
                                isOpenRules = 0;
                                topHolder.iv_game_details_account_msg_open_down.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_down_green));
                                topHolder.tv_game_details_account_msg_open_down.setText("????????????");
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
                                taskArray = gameTitleData.getJSONObject("awardList").getJSONArray("award0");

                                if (tv_game_details_item_type_one.getText().toString().contains("??????")) {
                                    ListType = "1";
                                } else if (tv_game_details_item_type_one.getText().toString().contains("??????")) {
                                    ListType = "2";
                                } else if (tv_game_details_item_type_one.getText().toString().contains("???")) {
                                    ListType = "3";
                                } else {
                                    ListType = "1";
                                }


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


                            ListType = "2";

                            try {

                                taskArray = gameTitleData.getJSONObject("awardList").getJSONArray("award1");

                                if (tv_game_details_item_type_two.getText().toString().contains("??????")) {
                                    ListType = "1";
                                } else if (tv_game_details_item_type_two.getText().toString().contains("??????")) {
                                    ListType = "2";
                                } else if (tv_game_details_item_type_two.getText().toString().contains("???")) {
                                    ListType = "3";
                                } else {
                                    ListType = "1";
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
                                taskArray = gameTitleData.getJSONArray("activityList");


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
                } catch (ParseException e) {
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


                        viewTypeTwoHolder.tv_item_game_details_fuli_msg.setText(single.getString("aname"));
                        //viewTypeTwoHolder.tv_item_game_details_fuli_num.setText(position + "");
                        viewTypeTwoHolder.tv_item_game_details_fuli_num.setText("" + position);


                        viewTypeTwoHolder.ll_item_game_details_fuli.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (isOpenChecked == 1) {
                                    isOpenChecked = 2;
                                    viewTypeTwoHolder.tv_item_game_details_fuli_open.setText("??????");
                                    viewTypeTwoHolder.tv_item_game_details_fuli_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_up_green));

                                    viewTypeTwoHolder.ll_item_game_details_fuli_list.setVisibility(View.VISIBLE);
                                    try {
                                        TypeThreeArray = single.getJSONArray("awardrecord");
                                        typeThreeAdapter = new TypeThreeAdapter(context);
                                        viewTypeTwoHolder.lv_item_game_details_fuli_list.setAdapter(typeThreeAdapter);
                                        Utils.setListViewHeightBasedOnChildren(viewTypeTwoHolder.lv_item_game_details_fuli_list);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    isOpenChecked = 1;
                                    viewTypeTwoHolder.tv_item_game_details_fuli_open.setText("??????");
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


                        holder.tv_item_game_details_type.setText(single.getString("progress"));

                        if (single.getString("progress").equals("???????????????")) {
                            holder.tv_item_game_details_type.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            holder.tv_item_game_details_msg.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                            holder.tv_item_game_details_money.setTextColor(getResources().getColor(R.color.yunbu_textgray));


                            //holder.tv_item_game_details_type.setBackground(getResources().getDrawable(R.drawable.light_purple_c30));
                        }

                        holder.tv_item_game_details_msg.setText(single.getString("event") + "(" + single.getString("groupname") + ")");
                        // holder.tv_item_game_details_money.setText("+" + single.getString("money") + single.getString("unit"));
                        //holder.tv_item_game_details_num.setText(position + "");
                        holder.tv_item_game_details_num.setText("" + position);

                        String getmoney = single.getString("pr").replace(",", "");
                        double finalmoney = 0;
                        if (ListType.equals("1")) {
                            double money = Double.valueOf(getmoney) - (Double.valueOf(getmoney) * SessionSingleton.getInstance().yunbuGameMoneyScale);
                            finalmoney = money - (money * SessionSingleton.getInstance().moneyScale);

                            BigDecimal sharemal = new BigDecimal(finalmoney);
                            finalmoney = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        } else if (ListType.equals("2")) {
                            double money = Double.valueOf(getmoney) - (Double.valueOf(getmoney) * SessionSingleton.getInstance().yunbuGameChangeScale);
                            finalmoney = money - (money * SessionSingleton.getInstance().moneyScaleGameRecharge);

                            BigDecimal sharemal = new BigDecimal(finalmoney);
                            finalmoney = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        }
                       // holder.tv_item_game_details_money.setText(Utils.getDoubleString(Double.valueOf(finalmoney)) + single.getString("unit"));
                        holder.tv_item_game_details_money.setText(finalmoney + SessionSingleton.getInstance().AccountSingle.getString("moneyUnit"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }


            return convertView;
        }

        class ViewTopHolder {
            ImageView iv_game_details_back, iv_game_details_image;
            TextView tv_game_details_title, tv_game_details_name, tv_game_details_size, tv_game_details_type, tv_game_details_money;
            TextView tv_game_details_time_one, tv_game_details_time_two, tv_game_details_time_three, tv_game_details_time_four;

            LinearLayout ll_game_details_title_background;

            TextView tv_game_details_registered_tishi;
            ImageView iv_game_details_registered_refresh;

            TextView tv_game_details_registered_title, tv_game_details_shiwanwanfei;
            LinearLayout ll_game_details_registered;
            TextView tv_game_details_registered_cant_msg;
            TextView tv_game_details_registered_msg_gameid, tv_game_details_registered_msg_gamename, tv_game_details_registered_msg_taskone, tv_game_details_registered_msg_tasktwo,
                    tv_game_details_registered_msg_taskthree, tv_game_details_registered_msg_taskfour, tv_game_details_registered_msg_taskfive;

            TextView tv_game_details_registered_msg;

            ImageView iv_game_details_account_msg_open_down;
            TextView tv_game_details_account_msg_open_down;
            LinearLayout ll_game_details_account_msg_rules;

            TextView tv_game_details_rules_qq;


        }

        class ViewTypeTwoHolder {
            TextView tv_item_game_details_fuli_msg, tv_item_game_details_fuli_open, tv_item_game_details_fuli_num;
            ImageView tv_item_game_details_fuli_image;
            LinearLayout ll_item_game_details_fuli_list,ll_item_game_details_fuli;
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

                holder.tv_item_game_details_fuli_item_id.setText(object.getString("merid"));
                // holder.tv_item_game_details_fuli_item_money.setText(object.getString("money"));
                holder.tv_item_game_details_fuli_item_unit.setText(object.getString("unit"));

                String money = object.getString("pr").replace(",", "");

                double finalmoney = Double.valueOf(money) - (Double.valueOf(money) * SessionSingleton.getInstance().yunbuGameGradeSerailMoneyScale);

                BigDecimal sharemal = new BigDecimal(finalmoney);
                finalmoney = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                holder.tv_item_game_details_fuli_item_money.setText(finalmoney+"");


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
                    holder.tv_item_game_details_fuli_item_num.setText(position + 1 + "");
                }
               // holder.tv_item_game_details_fuli_item_num.setText(position + 1 + "");
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


    //????????????
    private void XiQugame(String adid) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("adid", adid);
            params.put("ptype", "2");
            params.put("deviceid", deviceid);
            params.put("androidosv", androidosv);
            params.put("appid", SessionSingleton.getInstance().XiQuSingle.getString("key"));
            params.put("msaoaid", msaoaid);
            params.put("version", "2");
            params.put("keycode", keycode);
            params.put("appsign", account);
            HttpUtils.dogetHttpReqeust( "https://h5.wangzhuantianxia.com/try/API/try_api_adInfo?", params, new HttpUtils.StringCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        gameTitleData = object.getJSONObject("ADInfo");

                        if (gameTitleData.getJSONObject("awardList").length() < 1) {
                            showToast(1,"????????????????????????????????????????????????");
                            String limitgame =     SessionSingleton.getInstance().mysp .getString("limitgame", "") +gameTitleData.getJSONObject("baseInfo").getString("adName");
                            SessionSingleton.getInstance().mysp .edit().putString("limitgame", limitgame).commit();
                        }else{

                            if (gameTitleData.getJSONObject("awardList").length() == 0) {
                                taskArray = gameTitleData.getJSONArray("activityList");
                                ListType = "3";
                                tv_game_details_item_type_one_zhishiqi.setVisibility(View.GONE);
                                tv_game_details_item_type_two_zhishiqi.setVisibility(View.GONE);
                                tv_game_details_item_type_three_zhishiqi.setVisibility(View.VISIBLE);

                                tv_game_details_item_type_one.setTextSize(14);
                                tv_game_details_item_type_two.setTextSize(14);
                                tv_game_details_item_type_three.setTextSize(17);

                                tv_game_details_item_type_one.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                                tv_game_details_item_type_two.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                                tv_game_details_item_type_three.setTextColor(getResources().getColor(R.color.yunbu_textblack));

                            } else {
                                if (ListType.equals("1")) {
                                    taskArray = gameTitleData.getJSONObject("awardList").getJSONArray("award0");
                                } else if (ListType.equals("2")) {
                                    taskArray = gameTitleData.getJSONObject("awardList").getJSONArray("award1");
                                } else if (ListType.equals("3")) {
                                    taskArray = gameTitleData.getJSONArray("activityList");
                                }
                            }

                        }


                        mLoading.dismiss();
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFaileure(int code, Exception e) {
                    e.printStackTrace();

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    //????????????
    private void LoadXiQugame(String adid) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("adid", adid);
            params.put("ptype", "2");
            params.put("deviceid", deviceid);
            params.put("androidosv", androidosv);
            params.put("appid", SessionSingleton.getInstance().XiQuSingle.getString("key"));
            params.put("msaoaid", msaoaid);
            params.put("action", "UserClick");
            params.put("ctype", "1");
            params.put("keycode", keycode);
            params.put("appsign", account);
            HttpUtils.dogetHttpReqeust("https://h5.wangzhuantianxia.com/try/API/try_api_adClick?", params, new HttpUtils.StringCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        DownUrl = object.getString("APPUrl");
                        //DownUrl = object.getString("DownMethod");


                      /*  DownloadManager downloadManager = (DownloadManager) GameDetailsActivity.this.getSystemService(DOWNLOAD_SERVICE);
                        Uri uri = Uri.parse(DownUrl);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                        downloadManager.enqueue(request);*/
                        //initDownload(DownUrl);
                        bindService(DownUrl);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFaileure(int code, Exception e) {
                    e.printStackTrace();

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public void bindService(String apkUrl) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_URL, apkUrl);
        isBindService = context.bindService(intent, conn, BIND_AUTO_CREATE);
    }

    /**
     * ????????????????????????????????????apk
     */
    private void removeOldApk(String s) {
        //?????????????????????????????????
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
            XiQugame(adid);
        }
    }


    /**
     * ???????????????QQ????????????
     *
     * @param context ?????????
     * @param QQ      QQ??????
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




    //??????
    private void showToastTISHI() {

        builder = new AlertDialog.Builder(context);//???????????????

        inflater = getLayoutInflater();
        layout = inflater.inflate(R.layout.pop_yun_bu_game_detials_tishi, null);//?????????????????????
        builder.setView(layout);//????????????????????????
        dialog = builder.create();//????????????????????????

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

                if (limit == 1) {
                    tv_gamedetails_start_game.setText("????????????");

                } else {

                    if (Utils.checkAppInstalled(context, packgeName) == true) {

                        tv_gamedetails_start_game.setText("????????????");
                        ll_gamedetails_start_game.setVisibility(View.VISIBLE);
                        ll_gamedetails_load_game.setVisibility(View.GONE);

                        PackageManager packageManager = getPackageManager();
                        Intent intent = new Intent();
                        intent = packageManager.getLaunchIntentForPackage(packgeName);
                        if (intent == null) {
                            Toast.makeText(context, "?????????", Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(intent);
                        }
                    } else {
                        tv_gamedetails_start_game.setText("????????????");
                        ll_gamedetails_start_game.setVisibility(View.GONE);
                        ll_gamedetails_load_game.setVisibility(View.VISIBLE);
                        LoadXiQugame(adid);
                    }


                }
            }
        });


    }

    //??????
    private void showToast(final int status, String msg) {

        LayoutInflater la = LayoutInflater.from(context);
        View contentView = la.inflate(R.layout.pop_yun_bu_yun_bu_game_details_toast_show, null);//???????????????

        TextView tv_toast_msg = contentView.findViewById(R.id.tv_game_details_toast_msg);
        TextView tv_toast_close = contentView.findViewById(R.id.tv_game_details_toast_close);
        TextView  tv_game_details_toast_msg_title = contentView.findViewById(R.id.tv_game_details_toast_msg_title);

        if (status == 0) {
            tv_toast_msg.setText("???????????????" + account);
        } else {
            tv_toast_msg.setText(msg);
            tv_game_details_toast_msg_title.setVisibility(View.GONE);
            tv_toast_close.setText("??????");
        }


        tv_toast_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (status == 0) {
                    tosatPopuo.dismiss();

                    //???????????????????????????
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    // ?????????????????????ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", account);
                    // ???ClipData?????????????????????????????????
                    cm.setPrimaryClip(mClipData);

                    openQQChat(context, kefuQQ);
                }else{
                    tosatPopuo.dismiss();
                    finish();
                }

            }
        });


        tosatPopuo = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


        //??????PopupWindow?????????
        tosatPopuo.setFocusable(true);
        tosatPopuo.setClippingEnabled(false);
        //??????PopupWindow???????????????PopupWindow?????????
        tosatPopuo.setOutsideTouchable(true);
        //showAtLocation(View parent, int gravity, int x, int y)????????????????????????????????????????????????Gravity.CENTER?????????Gravity.BOTTOM???????????????????????????????????????
        tosatPopuo.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        tosatPopuo.update();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
