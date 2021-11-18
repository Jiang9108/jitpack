package com.lt.jitpacktest.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.utils.HttpUtils;
import com.lt.jitpacktest.utils.SessionSingleton;
import com.lt.jitpacktest.utils.Utils;
import com.lt.jitpacktest.xPullRefresh.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class YunBuWelfareActivity extends AppCompatActivity {
    private Context context;
    public Dialog mLoading;
    private PopupWindow tosatPopuo;

    private ImageView iv_welfare_bill_back;
    private XListView xlv_welfare_bill;

    private RelativeLayout rl_welfare_bill_title_background;
    private TextView tv_welfare_bill_title;

    private JSONArray welfareArray;
    private String chanelUserAccount, deviceid, xianwanSgin, msaoaid, androidosv;
    private WelfareBillAdapter adapter;
    private int page;

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

        setContentView(R.layout.activity_yun_bu_welfare);

        context = this;

        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);


        innitview();
    }

    private void innitview() {
        xlv_welfare_bill = findViewById(R.id.xlv_welfare_bill);
        iv_welfare_bill_back = findViewById(R.id.iv_welfare_bill_back);
        rl_welfare_bill_title_background = findViewById(R.id.rl_welfare_bill_title_background);
        tv_welfare_bill_title = findViewById(R.id.tv_welfare_bill_title);

        welfareArray = new JSONArray();


        try {
            deviceid = SessionSingleton.getInstance().AccountSingle.getString("deviceNumber");
            msaoaid = SessionSingleton.getInstance().AccountSingle.getString("oaId");
            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");
            androidosv = String.valueOf(Build.VERSION.SDK_INT);

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_welfare_bill_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                rl_welfare_bill_title_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());


                if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                    iv_welfare_bill_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                }else{
                    iv_welfare_bill_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }


            xianwanSgin = Utils.getMD5(SessionSingleton.getInstance().XianWanSingle.getString("key") + deviceid + msaoaid + androidosv + "2" + chanelUserAccount + SessionSingleton.getInstance().XianWanSingle.getString("secret"));


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        page = 1;
        getXWBillData(page);

        xlv_welfare_bill.setPullRefreshEnable(true);
        xlv_welfare_bill.setPullLoadEnable(true);
        adapter = new WelfareBillAdapter(context);
        xlv_welfare_bill.setXListViewListener(new XListView.IXListViewListener() {

            @Override
            public void onRefresh() {
                page = 1;
                mLoading.show();
                getXWBillData(page);

                Load();
            }

            @Override
            public void onLoadMore() {
                page = page + 1;
                mLoading.show();
                getXWBillData(page);
                Load();
            }

        });

        xlv_welfare_bill.setAdapter(adapter);

        iv_welfare_bill_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void Load() {
        xlv_welfare_bill.stopLoadMore();
        xlv_welfare_bill.stopRefresh();
    }

    //闲玩榜单列表
    private void getXWBillData(final int page) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("ptype", "2");
            params.put("androidosv", androidosv);
            params.put("msaoaid", msaoaid);
            params.put("appid", SessionSingleton.getInstance().XianWanSingle.getString("key"));
            params.put("deviceid", deviceid);
            params.put("appsign", chanelUserAccount);
            params.put("keycode", xianwanSgin);
            params.put("xwversion", "2");
            params.put("adtype", "0");
            params.put("pagepage", String.valueOf(page));
            HttpUtils.dogetHttpReqeust("https://h5.17xianwan.com/adwall/api/myWelfare?", params, new HttpUtils.StringCallback() {
                @Override
                public void onSuccess(String response) {
                    mLoading.dismiss();
                    try {
                        JSONObject returnJSONObject = new JSONObject(response);
                        if (returnJSONObject.getInt("status") == 0) {
                            JSONArray array = returnJSONObject.getJSONArray("items");
                            if (page <= 1) {
                                welfareArray = array;
                            } else {
                                for (int i = 0; i < array.length(); i++) {
                                    welfareArray.put(array.getJSONObject(i));
                                }
                            }

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

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    //闲玩榜单领取
    private void getXianWanbangdan(String drawAdid, String actid, String arank, String userId) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("ptype", "2");
            params.put("androidosv", androidosv);
            params.put("msaoaid", msaoaid);
            params.put("appid", SessionSingleton.getInstance().XianWanSingle.getString("key"));
            params.put("deviceid", deviceid);
            params.put("appsign", chanelUserAccount);
            params.put("keycode", xianwanSgin);
            params.put("xwversion", "2");
            params.put("adtype", "0");
            params.put("drawAdid", drawAdid);
            params.put("actid", actid);
            params.put("arank", arank);
            params.put("userIduserId", userId);
            HttpUtils.dogetHttpReqeust( "https://h5.17xianwan.com/adwall/api/drawWelfare?", params, new HttpUtils.StringCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        mLoading.dismiss();
                        JSONObject returnJSONObject = new JSONObject(response);
                        if (returnJSONObject.getInt("status") == 0) {
                            showToast("冲榜奖励领取成功，奖励将于1-2个工作日发放至您的提现账号上，请注意查收！");
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

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public class WelfareBillAdapter extends BaseAdapter {

        private LayoutInflater inflater;


        public WelfareBillAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return welfareArray.length();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_yun_bu_welfare, null);
            holder.tv_item_welfare_title = (TextView) convertView.findViewById(R.id.tv_item_welfare_title);
            holder.tv_item_welfare_status = (TextView) convertView.findViewById(R.id.tv_item_welfare_status);
            holder.tv_item_welfare_desc = (TextView) convertView.findViewById(R.id.tv_item_welfare_desc);
            holder.tv_item_welfare_money = (TextView) convertView.findViewById(R.id.tv_item_welfare_money);
            holder.tv_item_welfare_time = (TextView) convertView.findViewById(R.id.tv_item_welfare_time);
            holder.tv_item_welfare_receive = (TextView) convertView.findViewById(R.id.tv_item_welfare_receive);

            try {
                final JSONObject single = welfareArray.getJSONObject(position);

                holder.tv_item_welfare_title.setText(single.getString("adName"));

                holder.tv_item_welfare_desc.setText("【" + single.getString("event") + "】第" + single.getString("arank") + "名");
                holder.tv_item_welfare_money.setText("+" + single.getString("money") + single.getString("unit"));
                holder.tv_item_welfare_time.setText("审核时间：" + single.getString("itime"));

                //领奖状态 0：待审核 1：待领取[人工审核通过]，2：奖励发放中 3：已领取[人工已发放或系统发放] 4：领取超时；5：领取失败 6：审核不通过 7：人工审核通过待用户领取
                if (single.getInt("aStatus") == 0) {
                    holder.tv_item_welfare_status.setText("待审核");
                    holder.tv_item_welfare_receive.setVisibility(View.GONE);
                } else if (single.getInt("aStatus") == 1) {
                    holder.tv_item_welfare_status.setText("待领取");
                    holder.tv_item_welfare_receive.setVisibility(View.VISIBLE);
                } else if (single.getInt("aStatus") == 2) {
                    holder.tv_item_welfare_status.setText("奖励发放中");
                    holder.tv_item_welfare_receive.setVisibility(View.GONE);
                } else if (single.getInt("aStatus") == 3) {
                    holder.tv_item_welfare_status.setText("已领取");
                    holder.tv_item_welfare_receive.setVisibility(View.GONE);
                } else if (single.getInt("aStatus") == 4) {
                    holder.tv_item_welfare_status.setText("领取超时");
                    holder.tv_item_welfare_receive.setVisibility(View.VISIBLE);
                } else if (single.getInt("aStatus") == 5) {
                    holder.tv_item_welfare_status.setText("领取失败");
                    holder.tv_item_welfare_receive.setVisibility(View.VISIBLE);
                } else if (single.getInt("aStatus") == 6) {
                    holder.tv_item_welfare_status.setText("审核不通过");
                    holder.tv_item_welfare_receive.setVisibility(View.GONE);
                } else if (single.getInt("aStatus") == 7) {
                    holder.tv_item_welfare_status.setText("人工审核通过待用户领取");
                    holder.tv_item_welfare_receive.setVisibility(View.VISIBLE);
                }


                holder.tv_item_welfare_receive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            getXianWanbangdan(single.getString("adid"), single.getString("actid"), single.getString("arank"), single.getString("userId"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


            return convertView;
        }


        class ViewHolder {
            TextView tv_item_welfare_title, tv_item_welfare_status, tv_item_welfare_desc, tv_item_welfare_money;
            TextView tv_item_welfare_receive, tv_item_welfare_time;
        }

    }


    //提示
    private void showToast(final String message) {

        LayoutInflater la = LayoutInflater.from(context);
        View contentView = la.inflate(R.layout.pop_yun_bu_yun_toast_show, null);//自定义布局

        final TextView tv_toast_show__msg = contentView.findViewById(R.id.tv_toast_show__msg);
        TextView tv_toast_show_close = contentView.findViewById(R.id.tv_toast_show_close);
        ImageView iv_toast_show_close = contentView.findViewById(R.id.iv_toast_show_close);


        tv_toast_show__msg.setText(message);

        iv_toast_show_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tosatPopuo.dismiss();
            }
        });

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tosatPopuo.dismiss();
            }
        });


        tv_toast_show_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tosatPopuo.dismiss();
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

}
