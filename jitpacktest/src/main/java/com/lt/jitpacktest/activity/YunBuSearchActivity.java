package com.lt.jitpacktest.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.utils.HttpUtils;
import com.lt.jitpacktest.utils.SessionSingleton;
import com.lt.jitpacktest.utils.Utils;
import com.lt.jitpacktest.yunbuimageload.AsyncImageLoader;
import com.lt.jitpacktest.yunbuimageload.FileCache;
import com.lt.jitpacktest.yunbuimageload.MemoryCache;
import com.qubian.mob.QbManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class YunBuSearchActivity extends AppCompatActivity {
    private Context context;

    private ListView lv_search;
    private EditText et_yun_bu_search_content;
    private TextView tv_yun_bu_search_search, tv_yun_bu_search_keyword;
    private ImageView iv_yun_bu_search_back;
    private LinearLayout ll_yun_bu_search_no_data;

    private RelativeLayout rl_yun_bu_search_background;
    private TextView tv_yun_bu_search_title;

    public Dialog mLoading;
    private SearchAdapter adapter;
    private JSONArray searchListArray;
    private JSONObject searchGameSingle;

    private String searchType, searchWord = "";
    private String token, ChannelLoadSerail, chanelUserAccount, rewardUrl, searchGameUrl, gameListUrl;

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
            //decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_yun_bu_search);

        context = this;

        Intent intent = getIntent();
        searchType = intent.getStringExtra("SEARCHTYPE");


        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);


        initview();
        setlistener();
    }

    private void initview() {
        iv_yun_bu_search_back = findViewById(R.id.iv_yun_bu_search_back);
        lv_search = findViewById(R.id.lv_search);
        et_yun_bu_search_content = findViewById(R.id.et_yun_bu_search_content);
        tv_yun_bu_search_search = findViewById(R.id.tv_yun_bu_search_search);

        ll_yun_bu_search_no_data = findViewById(R.id.ll_yun_bu_search_no_data);
        tv_yun_bu_search_keyword = findViewById(R.id.tv_yun_bu_search_keyword);

        rl_yun_bu_search_background = findViewById(R.id.rl_yun_bu_search_background);
        tv_yun_bu_search_title = findViewById(R.id.tv_yun_bu_search_title);

        searchListArray = new JSONArray();

        try {
            token = SessionSingleton.getInstance().AccountSingle.getString("token");
            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");
            ChannelLoadSerail = SessionSingleton.getInstance().AccountSingle.getString("ChannelLoadSerail");

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_yun_bu_search_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                rl_yun_bu_search_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());


                if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                    iv_yun_bu_search_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                }else{
                    iv_yun_bu_search_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }


            gameListUrl = SessionSingleton.getInstance().requestBaseUrl + "gameCheckApi?";
            rewardUrl = SessionSingleton.getInstance().requestBaseUrl + "channelGetRewardListApi?";
            searchGameUrl = SessionSingleton.getInstance().requestBaseUrl + "adminGameManager?";
        } catch (JSONException e) {
            e.printStackTrace();
        }


        adapter = new SearchAdapter(context);
        lv_search.setAdapter(adapter);
    }

    private void setlistener() {
        iv_yun_bu_search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_yun_bu_search_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchWord = et_yun_bu_search_content.getText().toString();
                if (et_yun_bu_search_content.getText().toString().equals("")) {
                    Utils.showToast(context, "请输入正确的关键字");
                } else {
                    mLoading.show();
                    if (searchType.equals("game")) {
                        searchGameData(searchWord);
                    } else {
                        searchRewardData(searchWord);
                    }

                }
            }
        });

        //回车搜索
        et_yun_bu_search_content.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //是否是回车键
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //隐藏键盘
                    ((InputMethodManager) YunBuSearchActivity.this.getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(YunBuSearchActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    searchWord = et_yun_bu_search_content.getText().toString();
                    if (et_yun_bu_search_content.getText().toString().equals("")) {
                        Utils.showToast(context, "请输入正确的关键字");
                    } else {
                        mLoading.show();
                        if (searchType.equals("game")) {
                            searchGameData(searchWord);
                        } else {
                            searchRewardData(searchWord);
                        }
                    }

                }
                return false;
            }
        });

        lv_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (searchType.equals("game")) {
                        searchGameSingle = searchListArray.getJSONObject(position);
                        Long stoptime = Utils.data2(searchGameSingle.getString("stopTime"), "yyyy-MM-ddHH:mm:ss");
                        Long nowtime = System.currentTimeMillis();

                        if (nowtime < stoptime) {
                            loadInteractionAD();
                        } else {
                            Utils.showToast(context, "该游戏已结束");
                        }
                    } else {
                        SessionSingleton.getInstance().rewardDetailsSingle = searchListArray.getJSONObject(position);

                        Intent intent = new Intent(YunBuSearchActivity.this, YunBuRewardActivity.class);
                        intent.putExtra("REWARDTASKTYPE", "TASKLIST");
                        startActivity(intent);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class SearchAdapter extends BaseAdapter {
        private AsyncImageLoader imageLoader;//异步组件
        private LayoutInflater inflater;


        public SearchAdapter(Context context) {
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
            return searchListArray.length();
        }

        @Override
        public Object getItem(int position) {

            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGameHolder gameholder;
            ViewRewardHolder rewardholder;

            if (searchType.equals("game")) {
                gameholder = new ViewGameHolder();
                convertView = inflater.inflate(R.layout.item_yun_bu_game_type, null);
                gameholder.iv_item_game_head = (ImageView) convertView.findViewById(R.id.iv_item_game_head);
                gameholder.tv_item_game_title = (TextView) convertView.findViewById(R.id.tv_item_game_title);
                gameholder.tv_item_game_msg = (TextView) convertView.findViewById(R.id.tv_item_game_msg);
                gameholder.tv_item_game_money = (TextView) convertView.findViewById(R.id.tv_item_game_money);
                gameholder.tv_item_game_day = (TextView) convertView.findViewById(R.id.tv_item_game_day);

                try {
                    JSONObject single = searchListArray.getJSONObject(position);

                    gameholder.tv_item_game_title.setText(single.getString("title"));

                    Long stoptime = Utils.data2(single.getString("stopTime"), "yyyy-MM-ddHH:mm:ss");
                    Long nowtime = System.currentTimeMillis();

                    if (nowtime < stoptime) {
                        gameholder.tv_item_game_day.setText("结束时间：" + single.getString("stopTime"));
                        gameholder.tv_item_game_day.setBackground(getResources().getDrawable(R.drawable.orange_shape_c8_sanjiao));
                    } else {
                        gameholder.tv_item_game_day.setText("游戏已结束");
                        gameholder.tv_item_game_day.setBackground(getResources().getDrawable(R.drawable.gray_shape_c8_sanjiao));
                    }

                    gameholder.tv_item_game_msg.setText(single.getString("description"));

                    //double money = single.getDouble("totalMoney") * controlMoneyScale;
                    double money = single.getDouble("totalMoney");

                    gameholder.tv_item_game_money.setText(Utils.getDoubleString(money) + "元");


                    //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                    Bitmap bmp = imageLoader.loadBitmap(gameholder.iv_item_game_head, single.getString("logUrl"));
                    if (bmp == null) {
                        gameholder.iv_item_game_head.setImageResource(R.drawable.ic_load_iname);
                    } else {
                        gameholder.iv_item_game_head.setImageBitmap(bmp);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                rewardholder = new ViewRewardHolder();
                convertView = inflater.inflate(R.layout.item_yun_bu_game_type, null);
                rewardholder.iv_item_game_head = (ImageView) convertView.findViewById(R.id.iv_item_game_head);
                rewardholder.tv_item_game_title = (TextView) convertView.findViewById(R.id.tv_item_game_title);
                rewardholder.tv_item_game_msg = (TextView) convertView.findViewById(R.id.tv_item_game_msg);
                rewardholder.tv_item_game_money = (TextView) convertView.findViewById(R.id.tv_item_game_money);
                rewardholder.tv_item_game_day = (TextView) convertView.findViewById(R.id.tv_item_game_day);

                try {
                    JSONObject single = searchListArray.getJSONObject(position);

                    rewardholder.tv_item_game_title.setText(single.getString("productName"));
                    rewardholder.tv_item_game_day.setText("还剩" + single.getString("publicsurplusTimes") + "个");
                    rewardholder.tv_item_game_msg.setText(single.getString("taskType"));

                    //double money = single.getDouble("productPrice") * controlMoneyScale;
                    double money = single.getDouble("productPrice");

                    double finalmoney = money - (money * SessionSingleton.getInstance().moneyScaleReward);


                    rewardholder.tv_item_game_money.setText("+" + Utils.getDoubleString(finalmoney) + "元");


                    //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                    Bitmap bmp = imageLoader.loadBitmap(rewardholder.iv_item_game_head, single.getString("logurl"));
                    if (bmp == null) {
                        rewardholder.iv_item_game_head.setImageResource(R.drawable.ic_load_iname);
                    } else {
                        rewardholder.iv_item_game_head.setImageBitmap(bmp);
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return convertView;
        }

        class ViewGameHolder {
            TextView tv_item_game_title, tv_item_game_msg, tv_item_game_money, tv_item_game_day;
            ImageView iv_item_game_head;

        }

        class ViewRewardHolder {
            TextView tv_item_game_title, tv_item_game_msg, tv_item_game_money, tv_item_game_day;
            ImageView iv_item_game_head;

        }

    }

    //=============================================================================================================================================================


    public void searchGameData(final String keyWord) {
        Map<String, String> params = new HashMap<>();
        params.put("account", chanelUserAccount);
        params.put("token", token);
        params.put("type", "2");
        params.put("searchGameName", keyWord);
        HttpUtils.doHttpReqeust("POST", searchGameUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        if (returnJSONObject.getJSONArray("data").length() != 0) {
                            searchListArray = returnJSONObject.getJSONArray("data");

                            ll_yun_bu_search_no_data.setVisibility(View.GONE);
                        } else {
                            ll_yun_bu_search_no_data.setVisibility(View.VISIBLE);
                            tv_yun_bu_search_keyword.setText(keyWord);

                            GetGameList();
                        }

                    } else {
                        Utils.showToast(context, returnJSONObject.getString("msg"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFaileure(int code, Exception e) {
                e.printStackTrace();

            }
        });


    }

    public void searchRewardData(final String keyWord) {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("type", "2");
        params.put("serachContent", keyWord);
        HttpUtils.doHttpReqeust("POST", rewardUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        if (returnJSONObject.getJSONArray("data").length() != 0) {
                            searchListArray = returnJSONObject.getJSONArray("data");

                            ll_yun_bu_search_no_data.setVisibility(View.GONE);
                        } else {
                            ll_yun_bu_search_no_data.setVisibility(View.VISIBLE);
                            tv_yun_bu_search_keyword.setText(keyWord);

                            getRewardData();
                        }

                    } else {
                        Utils.showToast(context, returnJSONObject.getString("msg"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFaileure(int code, Exception e) {
                e.printStackTrace();

            }
        });

    }

    //=============================================================================================================================================================
    private void GetGameList() {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("apiType", "2");
        params.put("ChannelLoadSerail", ChannelLoadSerail);
        params.put("page", "1");
        params.put("limit", "20");
        HttpUtils.doHttpReqeust("POST", gameListUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    mLoading.dismiss();
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        searchListArray = returnJSONObject.getJSONArray("data");
                    } else {
                        Utils.showToast(context, returnJSONObject.getString("msg"));
                    }
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

    }

    public void getRewardData() {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("noShowFinishRewardTaskId", SessionSingleton.getInstance().noShowFinishRewardTaskId);
        params.put("type", "1");
        params.put("taskLoadType", "简单");
        params.put("page", "1");
        params.put("limit", "20");
        HttpUtils.doHttpReqeust("POST", rewardUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        searchListArray = returnJSONObject.getJSONArray("data");
                    } else {
                        Utils.showToast(context, returnJSONObject.getString("msg"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFaileure(int code, Exception e) {
                e.printStackTrace();

            }
        });

    }


    //=============================================================================================================================================================

    private void goGameDetails() {
        try {
            if (searchGameSingle.getString("channelName").equals("嘻趣")) {
                Intent intent = new Intent(YunBuSearchActivity.this, YunBuGameDetailsXiQuActivity.class);
                intent.putExtra("ADID", searchGameSingle.getString("adId"));
                intent.putExtra("KeFuQQ", SessionSingleton.getInstance().XiQuSingle.getString("serverQQ"));
                intent.putExtra("ShowFirst", SessionSingleton.getInstance().AccountSingle.getString("xiquShowFirstStep"));
                startActivity(intent);

            } else if (searchGameSingle.getString("channelName").equals("多游")) {
                Intent intent = new Intent(YunBuSearchActivity.this, YunBuGameDetailsDYActivity.class);
                intent.putExtra("ADID", searchGameSingle.getString("adId"));
                intent.putExtra("KeFuQQ", SessionSingleton.getInstance().DuoYouSingle.getString("serverQQ"));
                intent.putExtra("ShowFirst", SessionSingleton.getInstance().AccountSingle.getString("duoyouShowFirstStep"));
                startActivity(intent);
            } else if (searchGameSingle.getString("channelName").equals("聚享游")) {
                Intent intent = new Intent(YunBuSearchActivity.this, YunBuGameDetailsJuXiangWanActivity.class);
                intent.putExtra("ADID", searchGameSingle.getString("adId"));
                intent.putExtra("KeFuQQ", SessionSingleton.getInstance().JuXiangWanSingle.getString("serverQQ"));
                intent.putExtra("ShowFirst", SessionSingleton.getInstance().AccountSingle.getString("juxiangyouShowFirstStep"));
                startActivity(intent);
            } else if (searchGameSingle.getString("channelName").equals("闲玩")) {
                Intent intent = new Intent(YunBuSearchActivity.this, YunBuGameDetailsXianWanActivity.class);
                intent.putExtra("ADID", searchGameSingle.getString("adId"));
                intent.putExtra("KeFuQQ", SessionSingleton.getInstance().XianWanSingle.getString("serverQQ"));
                intent.putExtra("ShowFirst", SessionSingleton.getInstance().AccountSingle.getString("xianwanShowFirstStep"));
                startActivity(intent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadInteractionAD() {
        //加载插屏
        // codeId 平台申请的代码位id
        // channelNum 渠道号（可不填）
        // channelVersion 渠道版本号（填写渠道号时必填，否则会导致失效）
        // viewWidth 期望模板view的width（height自适应），默认值450（单位dp）
        // loadListener 回调
        QbManager.loadInteraction("1458726482611290151", "", "", 600, YunBuSearchActivity.this, new QbManager.InteractionLoadListener() {
            @Override
            public void onFail(String s) {
                Toast.makeText(YunBuSearchActivity.this, s, Toast.LENGTH_SHORT).show();
                goGameDetails();
            }

            @Override
            public void onDismiss() {
                goGameDetails();
            }

            @Override
            public void onVideoReady() {

            }

            @Override
            public void onVideoComplete() {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //释放所有激励视频资源，建议onDestroy中调用，也可在关闭回调中调用
        QbManager.destroyInteractionAll();
    }
}
