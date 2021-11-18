package com.lt.jitpacktest.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class YunBuRewardActivity extends AppCompatActivity {
    private Context context;

    private LinearLayout ll_reward_details_start_game, ll_reward_details_end_time, ll_reward_details_audit_end_time;
    private TextView tv_reward_details_tishi, tv_reward_details_audit_end_time_tishi, tv_reward_details_cancel_task, tv_reward_details_submit_task, tv_reward_details_audit_end_time_status;
    private TextView tv_reward_details_start_game;
    private LinearLayout ll_reward_details_qq, ll_reward_details_gonglue;

    private ImageView iv_reward_details_back;
    private TextView tv_reward_details_title;
    private LinearLayout ll_reward_details_background;
    private Dialog mLoading;

    private JSONObject RewardGingle;


    private int ISORNOJOIN = 0;
    private EditText et_reward_details_type_aduit_shuru_msg;

    private static int REQUEST_CAMERA = 1;
    private static int IMAGE_REQUEST_CODE = 2;
    private String paths, accountIsOrNoGetRewardUrl, startTaskUrl, checkTaskUrl, havingTaskUrl, productId, orderNumber,
            orderStatus = "开始任务", token, chanelUserAccount, channelAccount, kefuQQ = "3096561606", course = "https://www.baiydu.com/";
    private Long datetimeFailLong = 0L, nowdatetime = 0L, datetimeCheckFaillong = 0L;
    private CountDownTimer timer;

    private PopupWindow ShowImagePopupWindow;

    private XListView xlv_reward_details;
    private RewardDetailsAdapter adapter;
    private JSONArray rewardArray;

    private JSONArray RewardDetailsStepArray, RewardDetailsCheckArray;
    private int AuditImagePosition;

    private JSONObject submitCheckObject;
    private int checkIsOk = 0, isCheckSuccess = 0;

    private String source;
    private double controlMoneyScale = 0;

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

        setContentView(R.layout.activity_yun_bu_reward);

        context = this;

        Intent intent = getIntent();
        source = intent.getStringExtra("REWARDTASKTYPE");


        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);
        mLoading.show();

        initview();
        setlistener();
    }

    private void initview() {
        ll_reward_details_background = findViewById(R.id.ll_reward_details_background);
        tv_reward_details_title = findViewById(R.id.tv_reward_details_title);
        iv_reward_details_back = findViewById(R.id.iv_reward_details_back);

        xlv_reward_details = findViewById(R.id.xlv_reward_details);

        ll_reward_details_start_game = findViewById(R.id.ll_reward_details_start_game);
        ll_reward_details_end_time = findViewById(R.id.ll_reward_details_end_time);

        tv_reward_details_tishi = findViewById(R.id.tv_reward_details_tishi);
        tv_reward_details_cancel_task = findViewById(R.id.tv_reward_details_cancel_task);
        tv_reward_details_submit_task = findViewById(R.id.tv_reward_details_submit_task);

        tv_reward_details_audit_end_time_tishi = findViewById(R.id.tv_reward_details_audit_end_time_tishi);
        ll_reward_details_audit_end_time = findViewById(R.id.ll_reward_details_audit_end_time);
        tv_reward_details_audit_end_time_status = findViewById(R.id.tv_reward_details_audit_end_time_status);

        tv_reward_details_start_game = findViewById(R.id.tv_reward_details_start_game);
        ll_reward_details_qq = findViewById(R.id.ll_reward_details_qq);
        ll_reward_details_gonglue = findViewById(R.id.ll_reward_details_gonglue);

        try {
            RewardGingle = SessionSingleton.getInstance().rewardDetailsSingle;

            RewardDetailsStepArray = new JSONArray();
            RewardDetailsCheckArray = new JSONArray();
            submitCheckObject = new JSONObject();

            token = SessionSingleton.getInstance().AccountSingle.getString("token");
            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");
            channelAccount = SessionSingleton.getInstance().AccountSingle.getString("channelAccount");
            controlMoneyScale = SessionSingleton.getInstance().AccountSingle.getDouble("moneyScaleReward");

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_reward_details_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                ll_reward_details_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());


                if (SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon() == 0) {
                    iv_reward_details_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                } else {
                    iv_reward_details_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }


            JSONObject object = new JSONObject(RewardGingle.getString("taskStepAndCheckJSON"));
            rewardArray = new JSONArray();
            RewardDetailsStepArray = object.getJSONArray("stepArray");
            RewardDetailsCheckArray = object.getJSONArray("checkArray");
            for (int i = 0; i < RewardDetailsStepArray.length(); i++) {
                rewardArray.put(RewardDetailsStepArray.get(i));
            }
            for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                rewardArray.put(RewardDetailsCheckArray.get(i));
            }

            submitCheckObject.put("taskType", object.getString("taskType"));

            accountIsOrNoGetRewardUrl = SessionSingleton.getInstance().requestBaseUrl + "userGetRewardStatus?";
            startTaskUrl = SessionSingleton.getInstance().requestBaseUrl + "userGetRewardTaskApi?";
            checkTaskUrl = SessionSingleton.getInstance().requestBaseUrl + "userPostRewawrdTaskCheckApi?";
            havingTaskUrl = SessionSingleton.getInstance().requestBaseUrl + "userCheckRewardProductApi?";

            if (source.equals("HAVINGTASK")) {
                productId = RewardGingle.getString("productId");
                orderStatus = RewardGingle.getString("orderStatus");
                orderNumber = RewardGingle.getString("orderNumber");
                datetimeCheckFaillong = RewardGingle.getLong("datetimeCheckFaillong");
                datetimeFailLong = RewardGingle.getLong("datetimeFailLong");

                ISORNOJOIN = 1;
            } else if (source.equals("TASKLIST")) {
                productId = RewardGingle.getString("id");
            }


            if (source.equals("HAVINGTASK")) {
                getHavingRewardDetailse();
            } else if (source.equals("TASKLIST")) {
                AccountIsOrNoGetReward();
            }


            xlv_reward_details.setPullRefreshEnable(true);
            xlv_reward_details.setPullLoadEnable(false);
            adapter = new RewardDetailsAdapter(context);
            xlv_reward_details.setXListViewListener(new XListView.IXListViewListener() {

                @Override
                public void onRefresh() {

                    mLoading.show();
                    if (source.equals("HAVINGTASK")) {
                        getHavingRewardDetailse();
                    } else if (source.equals("TASKLIST")) {
                        AccountIsOrNoGetReward();
                    }


                    Load();
                }

                @Override
                public void onLoadMore() {

                }

            });


        } catch (JSONException e) {
            e.printStackTrace();
            mLoading.dismiss();
        }

    }

    private void setlistener() {
        /*返回*/
        iv_reward_details_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*立即开始*/
        tv_reward_details_start_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startRewardTask("3");


            }
        });

        /*客服*/
        ll_reward_details_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openQQChat(context, kefuQQ);
            }
        });

        /*攻略*/
        ll_reward_details_gonglue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");//打开手机自带浏览器
                intent.setData(Uri.parse(course));//设置
                //需要打开的网址
                startActivity(intent);
            }
        });

        /*取消任务*/
        tv_reward_details_cancel_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRewardTask("1");
            }
        });

        /*提交任务*/
        tv_reward_details_submit_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                        if (RewardDetailsCheckArray.getJSONObject(i).getString("type").equals("截图")) {
                            if (RewardDetailsCheckArray.getJSONObject(i).getString("checkUrl").equals("none")) {
                                Utils.showToast(context, "请添加验证图片！");
                            } else {
                                checkIsOk = checkIsOk + 1;
                            }
                        } else if (RewardDetailsCheckArray.getJSONObject(i).getString("type").equals("描述")) {
                            if (et_reward_details_type_aduit_shuru_msg.getText().toString().equals("")) {
                                Utils.showToast(context, "请输入文字审核内容！");
                            } else {
                                RewardDetailsCheckArray.getJSONObject(i).put("checkDescription", et_reward_details_type_aduit_shuru_msg.getText().toString());
                                checkIsOk = checkIsOk + 1;
                            }
                        }
                    }

                    /*for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                        if (RewardDetailsCheckArray.getJSONObject(i).getString("type").equals("截图")) {
                            RewardDetailsCheckArray.getJSONObject(i).put("checkUrl", "https://p0.qhimg.com/t0160daa44768b6906e.png");
                        } else if (RewardDetailsCheckArray.getJSONObject(i).getString("type").equals("描述")) {
                            RewardDetailsCheckArray.getJSONObject(i).put("checkDescription", et_reward_details_type_aduit_shuru_msg.getText().toString());
                        }

                    }
                    checkIsOk = RewardDetailsCheckArray.length();*/

                    if (checkIsOk == RewardDetailsCheckArray.length()) {
                        submitCheckObject.put("stepArray", RewardDetailsStepArray);
                        submitCheckObject.put("checkArray", RewardDetailsCheckArray);

                        submitCheckReward();

                        timer.cancel();
                    } else {
                        checkIsOk = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void Load() {
        xlv_reward_details.stopLoadMore();
        xlv_reward_details.stopRefresh();
    }

    public class RewardDetailsAdapter extends BaseAdapter {
        private AsyncImageLoader imageLoader;//异步组件
        private LayoutInflater inflater;


        public RewardDetailsAdapter(Context context) {
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
            return rewardArray.length() + 1;
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
            ViewTopHolder topHolder;

            int type = getItemViewType(position);
            try {
                if (type == 0) {
                    if (convertView == null) {
                        topHolder = new ViewTopHolder();
                        convertView = inflater.inflate(R.layout.item_yun_bu_rewrad_details_top, null);
                        topHolder.iv_reward_details_image = convertView.findViewById(R.id.iv_reward_details_image);
                        topHolder.tv_reward_details_name = convertView.findViewById(R.id.tv_reward_details_name);
                        topHolder.tv_reward_details_money = convertView.findViewById(R.id.tv_reward_details_money);
                        //  topHolder. tv_reward_details_msg = convertView.findViewById(R.id.tv_reward_details_msg);
                        topHolder.tv_reward_details_lable_one = convertView.findViewById(R.id.tv_reward_details_lable_one);
                        //topHolder. tv_reward_details_lable_two = convertView.findViewById(R.id.tv_reward_details_lable_two);

                        topHolder.tv_reward_details_shengyu = convertView.findViewById(R.id.tv_reward_details_shengyu);
                        topHolder.tv_reward_details_task_time = convertView.findViewById(R.id.tv_reward_details_task_time);
                        topHolder.tv_reward_details_audit_time = convertView.findViewById(R.id.tv_reward_details_audit_time);

                        topHolder.tv_reward_details_task_msg = convertView.findViewById(R.id.tv_reward_details_task_msg);

                        topHolder.tv_reward_details_step_one = convertView.findViewById(R.id.tv_reward_details_step_one);
                        topHolder.tv_reward_details_step_two = convertView.findViewById(R.id.tv_reward_details_step_two);
                        topHolder.tv_reward_details_step_three = convertView.findViewById(R.id.tv_reward_details_step_three);
                        topHolder.tv_reward_details_step_four = convertView.findViewById(R.id.tv_reward_details_step_four);
                        convertView.setTag(topHolder);
                    } else {
                        topHolder = (ViewTopHolder) convertView.getTag();
                    }


                    topHolder.tv_reward_details_name.setText(RewardGingle.getString("productName"));

                    double money =  RewardGingle.getDouble("productPrice")-(RewardGingle.getDouble("productPrice") * controlMoneyScale);


                    BigDecimal sharemal = new BigDecimal(money);
                    money = sharemal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();


                    topHolder.tv_reward_details_money.setText(Utils.getDoubleString(money) + "元");

                    //topHolder.tv_reward_details_msg.setText(RewardGingle.getString("taskType"));

                    topHolder.tv_reward_details_lable_one.setText(RewardGingle.getString("taskType"));
                    //topHolder.tv_reward_details_lable_two.setText(RewardGingle.getString("taskType"));

                    topHolder.tv_reward_details_shengyu.setText(RewardGingle.getString("publicsurplusTimes"));
                    topHolder.tv_reward_details_task_time.setText(RewardGingle.getString("finishTimesLimit"));
                    topHolder.tv_reward_details_audit_time.setText(RewardGingle.getString("checkTimesLimit"));


                    JSONObject shuoming = new JSONObject(RewardGingle.getString("elseContentJSON"));
                    kefuQQ = shuoming.getString("serverQQ");
                    course = shuoming.getString("course");
                    String alertContent = shuoming.getString("alertContent").replace("&", "\n");

                    topHolder.tv_reward_details_task_msg.setText(alertContent);

                    if (orderStatus.equals("开始任务")) {
                        topHolder.tv_reward_details_step_one.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_finish));
                        topHolder.tv_reward_details_step_two.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_three.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_four.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                    } else if (orderStatus.equals("进行中")) {
                        topHolder.tv_reward_details_step_one.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_two.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_finish));
                        topHolder.tv_reward_details_step_three.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_four.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                    } else if (orderStatus.equals("审核中")) {
                        topHolder.tv_reward_details_step_one.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_two.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_three.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_finish));
                        topHolder.tv_reward_details_step_four.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                    } else if (orderStatus.equals("审核成功")) {
                        topHolder.tv_reward_details_step_one.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_two.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_three.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_unfinish));
                        topHolder.tv_reward_details_step_four.setBackground(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_step_finish));
                    }

                    //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                    Bitmap bmp = imageLoader.loadBitmap(topHolder.iv_reward_details_image, RewardGingle.getString("logurl"));
                    if (bmp == null) {
                        topHolder.iv_reward_details_image.setImageResource(R.drawable.ic_load_iname);
                    } else {
                        topHolder.iv_reward_details_image.setImageBitmap(bmp);
                    }
                    //ImageLoader.getInstance().displayImage(RewardGingle.getString("logurl"), topHolder.iv_reward_details_image, customImageLoaderUtils_Circle.Corners(5));

                } else {
                    final JSONObject object = rewardArray.getJSONObject(position - 1);
                    if (object.getString("type").equals("二维码下载链接与描述")) {

                        convertView = inflater.inflate(R.layout.item_yun_bu_reward_details_item_erweima, null);
                        TextView tv_reward_details_type_erweima_num = convertView.findViewById(R.id.tv_reward_details_type_erweima_num);
                        TextView tv_reward_details_type_erweima_msg = convertView.findViewById(R.id.tv_reward_details_type_erweima_msg);
                        final ImageView iv_reward_details_type_erweima_image = convertView.findViewById(R.id.iv_reward_details_type_erweima_image);
                        TextView tv_reward_details_type_erweima_save = convertView.findViewById(R.id.tv_reward_details_type_erweima_save);

                        tv_reward_details_type_erweima_num.setText(String.valueOf(position));
                        tv_reward_details_type_erweima_msg.setText(object.getString("description"));


                        tv_reward_details_type_erweima_save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ISORNOJOIN == 1) {
                                    saveBitmap(((BitmapDrawable) iv_reward_details_type_erweima_image.getDrawable()).getBitmap());
                                    Toast.makeText(context, "二维码已保存到相册！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "请先报名后,再操作！", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        iv_reward_details_type_erweima_image.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                //弹出的“保存图片”的Dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(YunBuRewardActivity.this);
                                builder.setItems(new String[]{"保存图片"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveBitmap(((BitmapDrawable) iv_reward_details_type_erweima_image.getDrawable()).getBitmap());
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                        iv_reward_details_type_erweima_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    showBigPicture(object.getString("url"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                        Bitmap bmp = imageLoader.loadBitmap(iv_reward_details_type_erweima_image, object.getString("url"));
                        if (bmp == null) {
                            iv_reward_details_type_erweima_image.setImageResource(R.drawable.ic_load_iname);
                        } else {
                            iv_reward_details_type_erweima_image.setImageBitmap(bmp);
                        }

                   /*     ImageLoader.getInstance().displayImage(object.getString("url"), iv_reward_details_type_erweima_image,
                                customImageLoaderUtils_Circle.Corners(5));
                        ImageLoader.getInstance().displayImage(object.getString("url"), new ImageViewAware(iv_reward_details_type_erweima_image, false));
*/

                    } else if (object.getString("type").equals("下载链接与描述")) {

                        convertView = inflater.inflate(R.layout.item_yun_bu_reward_details_item_link, null);

                        TextView tv_reward_details_type_link_num = convertView.findViewById(R.id.tv_reward_details_type_link_num);
                        TextView tv_reward_details_type_link_msg = convertView.findViewById(R.id.tv_reward_details_type_link_msg);
                        TextView tv_reward_details_type_link_show_link = convertView.findViewById(R.id.tv_reward_details_type_link_show_link);
                        TextView tv_reward_details_type_link_copy = convertView.findViewById(R.id.tv_reward_details_type_link_copy);

                        final String downloadLink = object.getString("url");
                        tv_reward_details_type_link_num.setText(String.valueOf(position));
                        tv_reward_details_type_link_msg.setText(object.getString("description"));
                        tv_reward_details_type_link_show_link.setText(downloadLink);

                        tv_reward_details_type_link_copy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ISORNOJOIN == 1) {
                                    //获取剪贴板管理器：
                                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    // 创建普通字符型ClipData
                                    ClipData mClipData = ClipData.newPlainText("Label", downloadLink);

                                    // 将ClipData内容放到系统剪贴板里。
                                    cm.setPrimaryClip(mClipData);

                                    Toast.makeText(context, "链接已复制，快去注册吧！", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");//打开手机自带浏览器
                                    intent.setData(Uri.parse(downloadLink));//设置
                                    //需要打开的网址
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(context, "请先报名后,再操作！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else if (object.getString("type").equals("任务步骤纯文字")) {
                        convertView = inflater.inflate(R.layout.item_yun_bu_reward_details_item_step, null);

                        TextView iv_reward_details_type_step_num = convertView.findViewById(R.id.iv_reward_details_type_step_num);
                        TextView iv_reward_details_type_step_msg = convertView.findViewById(R.id.iv_reward_details_type_step_msg);
                        ImageView iv_reward_details_type_step_image_icon = convertView.findViewById(R.id.iv_reward_details_type_step_image_icon);
                        LinearLayout ll_reward_details_image = convertView.findViewById(R.id.ll_reward_details_image);
                        ImageView iv_reward_details_type_step_image = convertView.findViewById(R.id.iv_reward_details_type_step_image);
                        ImageView iv_reward_details_type_step_audit_image = convertView.findViewById(R.id.iv_reward_details_type_step_audit_image);
                        ImageView iv_reward_details_type_step_image_audit_show = convertView.findViewById(R.id.iv_reward_details_type_step_image_audit_show);

                        iv_reward_details_type_step_num.setText(String.valueOf(position));
                        iv_reward_details_type_step_msg.setText(object.getString("description"));

                        ll_reward_details_image.setVisibility(View.GONE);
                        iv_reward_details_type_step_image.setVisibility(View.GONE);
                        iv_reward_details_type_step_image_audit_show.setVisibility(View.GONE);
                        iv_reward_details_type_step_audit_image.setVisibility(View.GONE);
                    } else if (object.getString("type").equals("任务步骤图片链接与文字")) {
                        convertView = inflater.inflate(R.layout.item_yun_bu_reward_details_item_step, null);

                        TextView iv_reward_details_type_step_num = convertView.findViewById(R.id.iv_reward_details_type_step_num);
                        TextView iv_reward_details_type_step_msg = convertView.findViewById(R.id.iv_reward_details_type_step_msg);
                        ImageView iv_reward_details_type_step_image_icon = convertView.findViewById(R.id.iv_reward_details_type_step_image_icon);
                        LinearLayout ll_reward_details_image = convertView.findViewById(R.id.ll_reward_details_image);
                        final ImageView iv_reward_details_type_step_image = convertView.findViewById(R.id.iv_reward_details_type_step_image);
                        ImageView iv_reward_details_type_step_audit_image = convertView.findViewById(R.id.iv_reward_details_type_step_audit_image);
                        ImageView iv_reward_details_type_step_image_audit_show = convertView.findViewById(R.id.iv_reward_details_type_step_image_audit_show);

                        iv_reward_details_type_step_num.setText(String.valueOf(position));
                        iv_reward_details_type_step_msg.setText(object.getString("description"));

                        iv_reward_details_type_step_image_icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_icon_shuomingtu));

                        ll_reward_details_image.setVisibility(View.VISIBLE);
                        iv_reward_details_type_step_image.setVisibility(View.VISIBLE);
                        iv_reward_details_type_step_image_audit_show.setVisibility(View.GONE);
                        iv_reward_details_type_step_audit_image.setVisibility(View.GONE);


                        iv_reward_details_type_step_image.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                //弹出的“保存图片”的Dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(YunBuRewardActivity.this);
                                builder.setItems(new String[]{"保存图片"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveBitmap(((BitmapDrawable) iv_reward_details_type_step_image.getDrawable()).getBitmap());
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                        iv_reward_details_type_step_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    showBigPicture(object.getString("url"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                        //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                        Bitmap bmp = imageLoader.loadBitmap(iv_reward_details_type_step_image, object.getString("url"));
                        if (bmp == null) {
                            iv_reward_details_type_step_image.setImageResource(R.drawable.ic_load_iname);
                        } else {
                            iv_reward_details_type_step_image.setImageBitmap(bmp);
                        }
                        //ImageLoader.getInstance().displayImage(object.getString("url"), iv_reward_details_type_step_image, customImageLoaderUtils_Circle.Corners(5));

                    } else if (object.getString("type").equals("描述")) {
                        convertView = inflater.inflate(R.layout.item_yun_bu_reward_details_item_audit_text, null);

                        TextView tv_reward_details_type_aduit_num = convertView.findViewById(R.id.tv_reward_details_type_aduit_num);
                        TextView tv_reward_details_type_aduit_msg = convertView.findViewById(R.id.tv_reward_details_type_aduit_msg);
                        et_reward_details_type_aduit_shuru_msg = convertView.findViewById(R.id.et_reward_details_type_aduit_shuru_msg);
                        TextView tv_reward_details_type_aduit_finish = convertView.findViewById(R.id.tv_reward_details_type_aduit_finish);

                        tv_reward_details_type_aduit_num.setText(String.valueOf(position));
                        tv_reward_details_type_aduit_msg.setText(object.getString("description"));

                        if (orderStatus.equals("审核成功")) {
                            tv_reward_details_type_aduit_finish.setVisibility(View.VISIBLE);
                            et_reward_details_type_aduit_shuru_msg.setVisibility(View.GONE);
                            tv_reward_details_type_aduit_finish.setText(object.getString("checkDescription"));
                        } else if (orderStatus.equals("审核中")) {
                            tv_reward_details_type_aduit_finish.setVisibility(View.VISIBLE);
                            et_reward_details_type_aduit_shuru_msg.setVisibility(View.GONE);
                            tv_reward_details_type_aduit_finish.setText(object.getString("checkDescription"));
                        } else {
                            et_reward_details_type_aduit_shuru_msg.setVisibility(View.VISIBLE);
                            tv_reward_details_type_aduit_finish.setVisibility(View.GONE);
                        }

                    } else if (object.getString("type").equals("截图")) {
                        convertView = inflater.inflate(R.layout.item_yun_bu_reward_details_item_step, null);

                        TextView iv_reward_details_type_step_num = convertView.findViewById(R.id.iv_reward_details_type_step_num);
                        TextView iv_reward_details_type_step_msg = convertView.findViewById(R.id.iv_reward_details_type_step_msg);
                        LinearLayout ll_reward_details_image = convertView.findViewById(R.id.ll_reward_details_image);
                        ImageView iv_reward_details_type_step_image_icon = convertView.findViewById(R.id.iv_reward_details_type_step_image_icon);
                        final ImageView iv_reward_details_type_step_image = convertView.findViewById(R.id.iv_reward_details_type_step_image);
                        final ImageView iv_reward_details_type_step_audit_image = convertView.findViewById(R.id.iv_reward_details_type_step_audit_image);
                        final ImageView iv_reward_details_type_step_image_audit_show = convertView.findViewById(R.id.iv_reward_details_type_step_image_audit_show);

                        iv_reward_details_type_step_num.setText(String.valueOf(position));
                        iv_reward_details_type_step_msg.setText(object.getString("description"));

                        iv_reward_details_type_step_image_icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_details_icon_audit));

                        if (object.getString("checkUrl").equals("none")) {
                            iv_reward_details_type_step_image_audit_show.setVisibility(View.GONE);
                            iv_reward_details_type_step_audit_image.setVisibility(View.VISIBLE);
                        } else {
                            iv_reward_details_type_step_image_audit_show.setVisibility(View.VISIBLE);
                            iv_reward_details_type_step_audit_image.setVisibility(View.GONE);

                           /* ImageLoader.getInstance().displayImage(object.getString("checkUrl"), iv_reward_details_type_step_image_audit_show,
                                    customImageLoaderUtils_Circle.Corners(5));*/

                            //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                            Bitmap bmp = imageLoader.loadBitmap(iv_reward_details_type_step_image_audit_show, object.getString("checkUrl"));
                            if (bmp == null) {
                                iv_reward_details_type_step_image_audit_show.setImageResource(R.drawable.ic_load_iname);
                            } else {
                                iv_reward_details_type_step_image_audit_show.setImageBitmap(bmp);
                            }
                        }

                        ll_reward_details_image.setVisibility(View.VISIBLE);
                        iv_reward_details_type_step_image.setVisibility(View.VISIBLE);


                       /* ImageLoader.getInstance().displayImage(object.getString("url"), iv_reward_details_type_step_image,
                                customImageLoaderUtils_Circle.Corners(5));*/
                        //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
                        Bitmap bmp = imageLoader.loadBitmap(iv_reward_details_type_step_image, object.getString("url"));
                        if (bmp == null) {
                            iv_reward_details_type_step_image.setImageResource(R.drawable.ic_load_iname);
                        } else {
                            iv_reward_details_type_step_image.setImageBitmap(bmp);
                        }

                        iv_reward_details_type_step_image.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                //弹出的“保存图片”的Dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(YunBuRewardActivity.this);
                                builder.setItems(new String[]{"保存图片"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveBitmap(((BitmapDrawable) iv_reward_details_type_step_image.getDrawable()).getBitmap());
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                        iv_reward_details_type_step_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    showBigPicture(object.getString("url"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        iv_reward_details_type_step_image_audit_show.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                //弹出的“保存图片”的Dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(YunBuRewardActivity.this);
                                builder.setItems(new String[]{"保存图片"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveBitmap(((BitmapDrawable) iv_reward_details_type_step_image_audit_show.getDrawable()).getBitmap());
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                        iv_reward_details_type_step_image_audit_show.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    showBigPicture(object.getString("checkUrl"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                       /* Bitmap bitmap = BitmapFactory.decodeFile(AuditImageObject.getString(String.valueOf(currentId)));
                        iv_reward_details_type_step_image_audit_show.setImageBitmap(bitmap);*/


                        iv_reward_details_type_step_image_audit_show.setId(position - 1);
                        iv_reward_details_type_step_image_audit_show.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ISORNOJOIN == 1) {
                                    if (orderStatus.equals("进行中")) {
                                        AuditImagePosition = v.getId();
                                        getPhoto();
                                        // applyWritePermission();
                                    } else if (orderStatus.equals("审核中")) {
                                        try {
                                            showBigPicture(object.getString("checkUrl"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (orderStatus.equals("审核成功")) {
                                        try {
                                            showBigPicture(object.getString("checkUrl"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                } else {
                                    Toast.makeText(context, "请先报名后,再操作！", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        iv_reward_details_type_step_audit_image.setId(position - 1);
                        iv_reward_details_type_step_audit_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ISORNOJOIN == 1) {
                                    if (orderStatus.equals("进行中")) {

                                        AuditImagePosition = v.getId();
                                        getPhoto();
                                        // applyWritePermission();
                                    } else if (orderStatus.equals("审核中")) {
                                        try {
                                            showBigPicture(object.getString("checkUrl"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (orderStatus.equals("审核成功")) {
                                        try {
                                            showBigPicture(object.getString("checkUrl"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }


                                } else {
                                    Toast.makeText(context, "请先报名后,再操作！", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        class ViewTopHolder {
            ImageView iv_reward_details_image;
            TextView tv_reward_details_name, tv_reward_details_money, tv_reward_details_lable_one;
            TextView tv_reward_details_shengyu, tv_reward_details_task_time, tv_reward_details_audit_time;
            TextView tv_reward_details_task_msg;
            TextView tv_reward_details_step_one, tv_reward_details_step_two, tv_reward_details_step_three, tv_reward_details_step_four;
        }

    }

    public void AccountIsOrNoGetReward() {
        Map<String, String> params = new HashMap<>();
        params.put("channelAccount", channelAccount);
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("productId", productId);
        HttpUtils.doHttpReqeust("POST", accountIsOrNoGetRewardUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {

                        if (returnJSONObject.isNull("orderStatus")) {
                            ll_reward_details_start_game.setVisibility(View.VISIBLE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.GONE);

                            isCheckSuccess = 3;
                            gettimer(0L, 5000L);
                            timer.start();
                        } else {
                            ISORNOJOIN = 1;
                            orderStatus = returnJSONObject.getString("orderStatus");
                            orderNumber = returnJSONObject.getString("orderNumber");


                            if (orderStatus.equals("审核中")) {
                                ll_reward_details_start_game.setVisibility(View.GONE);
                                ll_reward_details_end_time.setVisibility(View.GONE);
                                ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                                isCheckSuccess = 1;

                                datetimeCheckFaillong = returnJSONObject.getLong("datetimeCheckFaillong");
                                //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                                nowdatetime = System.currentTimeMillis();
                                gettimer(nowdatetime, datetimeCheckFaillong);
                                timer.start();


                                rewardArray = new JSONArray();
                                JSONObject object = new JSONObject(returnJSONObject.getString("taskStepAndCheckJSON"));
                                RewardDetailsStepArray = object.getJSONArray("stepArray");
                                RewardDetailsCheckArray = object.getJSONArray("checkArray");
                                for (int i = 0; i < RewardDetailsStepArray.length(); i++) {
                                    rewardArray.put(RewardDetailsStepArray.get(i));
                                }
                                for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                                    rewardArray.put(RewardDetailsCheckArray.get(i));
                                }

                            } else if (orderStatus.equals("进行中")) {
                                ll_reward_details_start_game.setVisibility(View.GONE);
                                ll_reward_details_end_time.setVisibility(View.VISIBLE);
                                ll_reward_details_audit_end_time.setVisibility(View.GONE);

                                datetimeFailLong = returnJSONObject.getLong("datetimeFailLong");
                                //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                                nowdatetime = System.currentTimeMillis();
                                gettimer(nowdatetime, datetimeFailLong);
                                timer.start();

                                isCheckSuccess = 0;
                            } else if (orderStatus.equals("审核成功")) {
                                ll_reward_details_start_game.setVisibility(View.GONE);
                                ll_reward_details_end_time.setVisibility(View.GONE);
                                ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                                tv_reward_details_audit_end_time_tishi.setVisibility(View.GONE);
                                tv_reward_details_audit_end_time_status.setText("已完成");

                                rewardArray = new JSONArray();
                                JSONObject object = new JSONObject(returnJSONObject.getString("taskStepAndCheckJSON"));
                                RewardDetailsStepArray = object.getJSONArray("stepArray");
                                RewardDetailsCheckArray = object.getJSONArray("checkArray");
                                for (int i = 0; i < RewardDetailsStepArray.length(); i++) {
                                    rewardArray.put(RewardDetailsStepArray.get(i));
                                }
                                for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                                    rewardArray.put(RewardDetailsCheckArray.get(i));
                                }


                            }


                        }

                    } else {
                        Utils.showToast(context, returnJSONObject.getString("msg"));
                    }
                    xlv_reward_details.setAdapter(adapter);
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

    /*接单/取消订单*/
    public void startRewardTask(final String apiType) {
        Map<String, String> params = new HashMap<>();
        params.put("channelAccount", channelAccount);
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("productId", productId);
        params.put("apiType", apiType);
        HttpUtils.doHttpReqeust("POST", startTaskUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        if (apiType.equals("3")) {
                     /*       SessionSingleton.getInstance().noShowFinishRewardTaskId = SessionSingleton.getInstance().noShowFinishRewardTaskId
                                    + RewardGingle.getString("id") + "&";*/


                            ISORNOJOIN = 1;
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.VISIBLE);
                            ll_reward_details_audit_end_time.setVisibility(View.GONE);

                            isCheckSuccess = 0;
                            orderStatus = "进行中";

                            orderNumber = returnJSONObject.getString("orderNumber");
                            datetimeCheckFaillong = returnJSONObject.getLong("datetimeCheckFaillong");

                            datetimeFailLong = returnJSONObject.getLong("datetimeFailLong");
                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeFailLong);
                            timer.start();

                        } else if (apiType.equals("1")) {
              /*              String taskid = RewardGingle.getString("id") + "&";
                            SessionSingleton.getInstance().noShowFinishRewardTaskId = SessionSingleton.getInstance().noShowFinishRewardTaskId.
                                    replace(taskid, "");*/


                            ll_reward_details_start_game.setVisibility(View.VISIBLE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.GONE);
                            timer.cancel();
                        } else if (apiType.equals("2")) {
                            ISORNOJOIN = 1;
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.VISIBLE);

                            isCheckSuccess = 0;
                            orderStatus = "进行中";

                            orderNumber = returnJSONObject.getString("orderNumber");
                            datetimeCheckFaillong = returnJSONObject.getLong("datetimeCheckFaillong");

                            datetimeFailLong = returnJSONObject.getLong("datetimeFailLong");
                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeFailLong);
                            timer.start();
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Utils.showToast(context, returnJSONObject.getString("msg"));
                    }

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


    public void submitCheckReward() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("chanelUserAccount", chanelUserAccount);
            params.put("token", token);
            params.put("taskStepAndCheckJSON", URLEncoder.encode(submitCheckObject.toString(), "UTF-8"));
            params.put("productId", productId);
            params.put("OrderId", orderNumber);
            HttpUtils.doHttpReqeust("POST", checkTaskUrl, params, new HttpUtils.StringCallback() {
                @Override
                public void onSuccess(String response) {
                    mLoading.dismiss();
                    try {
                        JSONObject returnJSONObject = new JSONObject(response);
                        if (returnJSONObject.getString("status").equals("success")) {
                            isCheckSuccess = 1;
                            datetimeCheckFaillong = returnJSONObject.getLong("datetimeCheckFaillong");

                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                            tv_reward_details_audit_end_time_tishi.setVisibility(View.VISIBLE);

                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeCheckFaillong);
                            timer.start();

                        } else {
                            Utils.showToast(context, returnJSONObject.getString("msg"));
                        }
                        xlv_reward_details.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFaileure(int code, Exception e) {
                    e.printStackTrace();

                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*我的参与跳转进来*/
    public void getHavingRewardDetailse() {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("productId", productId);
        HttpUtils.doHttpReqeust("POST", havingTaskUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnObject = new JSONObject(response);
                    JSONObject returnJSONObject = returnObject.getJSONObject("productJsonObject");
                    if (returnObject.getString("status").equals("success")) {

                        if (orderStatus.equals("审核中")) {
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                            isCheckSuccess = 1;


                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeCheckFaillong);
                            timer.start();

                        } else if (orderStatus.equals("进行中")) {
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.VISIBLE);
                            ll_reward_details_audit_end_time.setVisibility(View.GONE);


                            isCheckSuccess = 0;

                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeFailLong);
                            timer.start();

                        } else if (orderStatus.equals("审核成功")) {
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                            tv_reward_details_audit_end_time_tishi.setVisibility(View.GONE);
                            tv_reward_details_audit_end_time_status.setText("已完成");
                        }

                        JSONObject elseContentJSON = new JSONObject(returnJSONObject.getString("elseContentJSON"));
                        RewardGingle.put("elseContentJSON", elseContentJSON);
                        RewardGingle.put("productPrice", returnJSONObject.getString("productPrice"));
                        RewardGingle.put("finishTimesLimit", returnJSONObject.getString("finishTimesLimit"));
                        RewardGingle.put("publicsurplusTimes", returnJSONObject.getString("publicsurplusTimes"));
                        RewardGingle.put("checkTimesLimit", returnJSONObject.getString("checkTimesLimit"));

                       /* rewardArray = new JSONArray();
                        JSONObject object = new JSONObject(returnJSONObject.getString("taskStepAndCheckJSON"));
                        RewardDetailsStepArray = object.getJSONArray("stepArray");
                        RewardDetailsCheckArray = object.getJSONArray("checkArray");
                        for (int i = 0; i < RewardDetailsStepArray.length(); i++) {
                            rewardArray.put(RewardDetailsStepArray.get(i));
                        }
                        for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                            rewardArray.put(RewardDetailsCheckArray.get(i));
                        }*/

                    } else {
                        Utils.showToast(context, "任务打开失败！");
                    }
                    xlv_reward_details.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaileure(int code, Exception e) {
                e.printStackTrace();

            }
        });

/*        OkHttpUtils.post().url(havingTaskUrl)
                .addParams("chanelUserAccount", chanelUserAccount)
                .addParams("token", token)
                .addParams("productId", productId)
                .build().execute(new StringCallback() {

            @Override
            public void onBefore(Request request, int id) {


            }

            @Override
            public void onAfter(int id) {

            }

            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();

            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onResponse(String response, int id) {
                mLoading.dismiss();
                try {
                    JSONObject returnObject = new JSONObject(response);
                    JSONObject returnJSONObject = returnObject.getJSONObject("productJsonObject");
                    if (returnObject.getString("status").equals("success")) {

                        if (orderStatus.equals("审核中")) {
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                            isCheckSuccess = 1;


                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeCheckFaillong);
                            timer.start();

                        } else if (orderStatus.equals("进行中")) {
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.VISIBLE);
                            ll_reward_details_audit_end_time.setVisibility(View.GONE);


                            isCheckSuccess = 0;

                            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
                            nowdatetime = System.currentTimeMillis();
                            gettimer(nowdatetime, datetimeFailLong);
                            timer.start();

                        } else if (orderStatus.equals("审核成功")) {
                            ll_reward_details_start_game.setVisibility(View.GONE);
                            ll_reward_details_end_time.setVisibility(View.GONE);
                            ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                            tv_reward_details_audit_end_time_tishi.setVisibility(View.GONE);
                            tv_reward_details_audit_end_time_status.setText("已完成");
                        }

                        JSONObject elseContentJSON = new JSONObject(returnJSONObject.getString("elseContentJSON"));
                        RewardGingle.put("elseContentJSON", elseContentJSON);
                        RewardGingle.put("productPrice", returnJSONObject.getString("productPrice"));
                        RewardGingle.put("finishTimesLimit", returnJSONObject.getString("finishTimesLimit"));
                        RewardGingle.put("publicsurplusTimes", returnJSONObject.getString("publicsurplusTimes"));
                        RewardGingle.put("checkTimesLimit", returnJSONObject.getString("checkTimesLimit"));

                        rewardArray = new JSONArray();
                        JSONObject object = new JSONObject(returnJSONObject.getString("taskStepAndCheckJSON"));
                        RewardDetailsStepArray = object.getJSONArray("stepArray");
                        RewardDetailsCheckArray = object.getJSONArray("checkArray");
                        for (int i = 0; i < RewardDetailsStepArray.length(); i++) {
                            rewardArray.put(RewardDetailsStepArray.get(i));
                        }
                        for (int i = 0; i < RewardDetailsCheckArray.length(); i++) {
                            rewardArray.put(RewardDetailsCheckArray.get(i));
                        }

                    } else {
                        Utils.showToast(context, "任务打开失败！");
                    }
                    xlv_reward_details.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        });*/

    }

    //保存图片
    public String saveBitmap(Bitmap bitmap) {
        try {

            String path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/yunbu/";
            File outputFile = new File(path, "qcode.png");


            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);


            out.flush();
            out.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    outputFile.getAbsolutePath(), "qcode.png", null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(outputFile);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            return outputFile.getPath();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    private void getPhoto() {
        //在这里跳转到手机系统相册里面
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

/*    private void checkNeedPermissions() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(YunBuRewardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    YunBuRewardActivity.this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else {
            getPhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoto();
        } else {
            // 没有获取 到权限，从新请求，或者关闭app
            Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show();
        }
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri selectedImage = data.getData(); //获取系统返回的照片的Uri

                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null); //从系统表中查询指定Uri对应的照片

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                paths = cursor.getString(columnIndex); //获取照片路径
                cursor.close();


                Utils.compressImage(context, paths, 320, 640);
                uploadImage();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //图片上传方法
    public void uploadImage() {
        String url = SessionSingleton.getInstance().requestBaseUrl + "userUploadImageShareApi?";
        // 普通参数
        HashMap<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("1", "1");


        HttpUtils.uploadForm(params, chanelUserAccount + "&" + token + "&" + "1",
                SessionSingleton.getInstance().uploadImageFile, chanelUserAccount + ".png", url, new HttpUtils.StringCallback() {
                    @Override
                    public void onSuccess(String response) {
                        mLoading.dismiss();

                        try {
                            JSONObject returnJSONObject = new JSONObject(response);
                            if (returnJSONObject.getString("status").equals("success")) {

                                JSONObject object = rewardArray.getJSONObject(AuditImagePosition);
                                object.put("checkUrl", returnJSONObject.getString("url"));

                                int length = AuditImagePosition - RewardDetailsStepArray.length();
                                RewardDetailsCheckArray.put(length, object);
                            } else {
                                Toast.makeText(context, "图片上传出错:" + returnJSONObject.getString("msg"), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFaileure(int code, Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "图片上传出错:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

     /*   HttpUtils.doFileHttpReqeust(url, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();

                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {

                        JSONObject object = rewardArray.getJSONObject(AuditImagePosition);
                        object.put("checkUrl", returnJSONObject.getString("url"));

                        int length = AuditImagePosition - RewardDetailsStepArray.length();
                        RewardDetailsCheckArray.put(length, object);
                    } else {
                        Toast.makeText(context, "图片上传出错:" + returnJSONObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFaileure(int code, Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "图片上传出错:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/



/*        OkHttpUtils.post().url(url).
                addFile(chanelUserAccount + "&" + token + "&" + "1",
                        chanelUserAccount + ".png", SessionSingleton.getInstance().uploadImageFile)
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {

            }

            @Override
            public void onAfter(int id) {

            }

            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                Toast.makeText(context, "图片上传出错:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                mLoading.dismiss();

                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {

                        JSONObject object = rewardArray.getJSONObject(AuditImagePosition);
                        object.put("checkUrl", returnJSONObject.getString("url"));

                        int length = AuditImagePosition - RewardDetailsStepArray.length();
                        RewardDetailsCheckArray.put(length, object);
                    } else {
                        Toast.makeText(context, "图片上传出错:" + returnJSONObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }
        });*/


    }


    private void gettimer(Long time1, Long time2) {
        timer = new CountDownTimer(time2 - time1, 1000) {
            public void onTick(long millisUntilFinished) {
                if (isCheckSuccess == 0) {
                    tv_reward_details_tishi.setText("请在" + formatLongToTimeStr(millisUntilFinished) + "内完成提交，逾期自动取消");
                } else if (isCheckSuccess == 3) {
                    tv_reward_details_start_game.setText("立即报名（" + millisUntilFinished / 1000 + "）");
                    tv_reward_details_start_game.setEnabled(false);
                } else {
                    tv_reward_details_audit_end_time_tishi.setText("任务将在" + formatLongToTimeStr(millisUntilFinished) + "内完成审核，逾期将自动审核成功");
                }

            }

            public void onFinish() {
                timer.cancel();
                if (isCheckSuccess == 0) {
                    ll_reward_details_start_game.setVisibility(View.VISIBLE);
                    ll_reward_details_end_time.setVisibility(View.GONE);
                    ll_reward_details_audit_end_time.setVisibility(View.GONE);

             /*       try {
                        String taskid = RewardGingle.getString("id") + "&";
                        SessionSingleton.getInstance().noShowFinishRewardTaskId = SessionSingleton.getInstance().noShowFinishRewardTaskId.
                                replace(taskid, "");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/

                } else if (isCheckSuccess == 3) {
                    tv_reward_details_start_game.setText("立即报名");
                    tv_reward_details_start_game.setEnabled(true);
                } else {
                    ll_reward_details_start_game.setVisibility(View.GONE);
                    ll_reward_details_end_time.setVisibility(View.GONE);
                    ll_reward_details_audit_end_time.setVisibility(View.VISIBLE);
                    tv_reward_details_audit_end_time_tishi.setVisibility(View.GONE);
                    tv_reward_details_audit_end_time_status.setText("已完成");
                }

            }
        };
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }

    }


    public String formatLongToTimeStr(Long millisecond) {
        int hour = (int) ((millisecond / 1000) / 60 / 60);
        int minute = (int) ((millisecond / 1000) / 60);
        int second = (int) ((millisecond / 1000) % 60);
        //second = millisecond.intValue() ;
        if (second > 60) {
            minute = second / 60;   //取整
            second = second % 60;   //取余
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        String strtime = hour + "：" + minute + "：" + second;
        return strtime;
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

    //显示弹窗图片
    private void showBigPicture(String url) {
        if (ShowImagePopupWindow == null) {
            LayoutInflater la = LayoutInflater.from(context);
            View contentView = la.inflate(R.layout.pop_yun_bu_show_big_pic, null);//自定义布局

            final ImageView iv_pop_show_bigpic = contentView.findViewById(R.id.iv_pop_show_bigpic);

            MemoryCache mcache = new MemoryCache();//内存缓存
            String paht = getApplicationContext().getFilesDir().getAbsolutePath();
            File cacheDir = new File(paht, "yunbucache");//缓存根目录
            FileCache fcache = new FileCache(context, cacheDir, "yunbuimage");//文件缓存
            AsyncImageLoader imageLoader = new AsyncImageLoader(context, mcache, fcache);

            //ImageLoader.getInstance().displayImage(url, new ImageViewAware(iv_pop_show_bigpic, false));
            //异步加载图片，先从一级缓存、再二级缓存、最后网络获取图片
            Bitmap bmp = imageLoader.loadBitmap(iv_pop_show_bigpic, url);
            if (bmp == null) {
                iv_pop_show_bigpic.setImageResource(R.drawable.ic_load_iname);
            } else {
                iv_pop_show_bigpic.setImageBitmap(bmp);
            }

            //大图的点击事件（点击让他消失）
            iv_pop_show_bigpic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowImagePopupWindow.dismiss();
                    ShowImagePopupWindow = null;
                }
            });

            //大图的长按监听
            iv_pop_show_bigpic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //弹出的“保存图片”的Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(YunBuRewardActivity.this);
                    builder.setItems(new String[]{"保存图片"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveBitmap(((BitmapDrawable) iv_pop_show_bigpic.getDrawable()).getBitmap());
                        }
                    });
                    builder.show();
                    return true;
                }
            });

            ShowImagePopupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }

        //设置PopupWindow的焦点
        ShowImagePopupWindow.setFocusable(true);
        //点击PopupWindow之外的地方PopupWindow会消失
        ShowImagePopupWindow.setOutsideTouchable(true);
        //showAtLocation(View parent, int gravity, int x, int y)：相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
        ShowImagePopupWindow.showAtLocation(YunBuRewardActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        ShowImagePopupWindow.update();
    }

}
