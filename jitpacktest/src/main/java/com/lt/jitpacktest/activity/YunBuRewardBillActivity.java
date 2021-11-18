package com.lt.jitpacktest.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;


public class YunBuRewardBillActivity extends AppCompatActivity {
    private Context context;
    public Dialog mLoading;
    private XListView xlv_reward_bill;
    private ImageView iv_reward_bill_back;
    private RelativeLayout rl_reward_bill_title_background;
    private TextView tv_reward_bill_title;
    private RewardBillAdapter adapter;
    private JSONArray rewardBillArray;

    private String billUrl, token, chanelUserAccount;
    private int pageIndex;

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

        setContentView(R.layout.activity_yun_bu_reward_bill);


        context = this;

        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);

        innitview();
    }


    private void innitview() {
        xlv_reward_bill = findViewById(R.id.xlv_reward_bill);
        iv_reward_bill_back = findViewById(R.id.iv_reward_bill_back);
        rl_reward_bill_title_background = findViewById(R.id.rl_reward_bill_title_background);
        tv_reward_bill_title = findViewById(R.id.tv_reward_bill_title);

        rewardBillArray = new JSONArray();

        billUrl = SessionSingleton.getInstance().requestBaseUrl + "channelUserChanageMoneyRecordApi?";
        try {
            token = SessionSingleton.getInstance().AccountSingle.getString("token");
            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_reward_bill_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                rl_reward_bill_title_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());



                if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                    iv_reward_bill_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                }else{
                    iv_reward_bill_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }


        pageIndex = 1;
        getBillData(pageIndex);

        xlv_reward_bill.setPullRefreshEnable(true);
        xlv_reward_bill.setPullLoadEnable(true);
        adapter = new RewardBillAdapter(context);
        xlv_reward_bill.setXListViewListener(new XListView.IXListViewListener() {

            @Override
            public void onRefresh() {
                pageIndex = 1;
                mLoading.show();
                getBillData(pageIndex);

                Load();
            }

            @Override
            public void onLoadMore() {
                pageIndex = pageIndex + 1;
                mLoading.show();
                getBillData(pageIndex);
                Load();
            }

        });

        xlv_reward_bill.setAdapter(adapter);

        iv_reward_bill_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void Load() {
        xlv_reward_bill.stopLoadMore();
        xlv_reward_bill.stopRefresh();
    }

    public class RewardBillAdapter extends BaseAdapter {

        private LayoutInflater inflater;


        public RewardBillAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return rewardBillArray.length();
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
            convertView = inflater.inflate(R.layout.item_yun_bu_reward_money_item, null);
            holder.tv_item_reward_money_title = (TextView) convertView.findViewById(R.id.tv_item_reward_money_title);
            holder.tv_item_reward_money_money = (TextView) convertView.findViewById(R.id.tv_item_reward_money_money);
            holder.tv_item_reward_money_msg = (TextView) convertView.findViewById(R.id.tv_item_reward_money_msg);
            holder.tv_item_reward_money_day = (TextView) convertView.findViewById(R.id.tv_item_reward_money_day);

            try {
                JSONObject single = rewardBillArray.getJSONObject(position);

                holder.tv_item_reward_money_title.setText(single.getString("shareName"));
                holder.tv_item_reward_money_money.setText(Utils.getDoubleString(single.getDouble("addMoney")) + "元");
                holder.tv_item_reward_money_msg.setText(single.getString("description"));
                holder.tv_item_reward_money_day.setText(single.getString("createTimes"));


            } catch (Exception e) {
                e.printStackTrace();
            }


            return convertView;
        }


        class ViewHolder {
            TextView tv_item_reward_money_title, tv_item_reward_money_money, tv_item_reward_money_msg, tv_item_reward_money_day;
        }

    }


    public void getBillData(final int pageIndex) {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("limit", "10");
        params.put("page", String.valueOf(pageIndex));
        HttpUtils.doHttpReqeust("POST", billUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        JSONArray array = returnJSONObject.getJSONArray("data");
                        if (pageIndex <= 1) {
                            rewardBillArray = array;
                        } else {
                            for (int i = 0; i < array.length(); i++) {
                                rewardBillArray.put(array.getJSONObject(i));
                            }
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
}
