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


public class YunBuWithdrawalBillActivity extends AppCompatActivity {
    private Context context;
    public Dialog mLoading;
    private XListView xlv_withdrawal_bill;
    private ImageView iv_withdrawal_bill_back;
    private WithdrawalBillAdapter adapter;
    private JSONArray billArray;

    private TextView tv_withdrawal_bill_title;
    private RelativeLayout rl_withdrawal_bill_background;

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

        setContentView(R.layout.activity_yun_bu_withdrawal_bill);

        context = this;

        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);

        innitview();
    }

    private void innitview() {
        xlv_withdrawal_bill = findViewById(R.id.xlv_withdrawal_bill);
        iv_withdrawal_bill_back = findViewById(R.id.iv_withdrawal_bill_back);
        tv_withdrawal_bill_title = findViewById(R.id.tv_withdrawal_bill_title);
        rl_withdrawal_bill_background = findViewById(R.id.rl_withdrawal_bill_background);


        billArray = new JSONArray();

        billUrl = SessionSingleton.getInstance().requestBaseUrl + "channelUserShareWithdrawApi?";
        try {
            token = SessionSingleton.getInstance().AccountSingle.getString("token");
            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_withdrawal_bill_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                rl_withdrawal_bill_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());

                if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                    iv_withdrawal_bill_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                }else{
                    iv_withdrawal_bill_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }


        pageIndex = 1;
        getBillData(pageIndex);

        xlv_withdrawal_bill.setPullRefreshEnable(true);
        xlv_withdrawal_bill.setPullLoadEnable(true);
        adapter = new WithdrawalBillAdapter(context);
        xlv_withdrawal_bill.setXListViewListener(new XListView.IXListViewListener() {

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

        xlv_withdrawal_bill.setAdapter(adapter);

        iv_withdrawal_bill_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void Load() {
        xlv_withdrawal_bill.stopLoadMore();
        xlv_withdrawal_bill.stopRefresh();
    }

    public class WithdrawalBillAdapter extends BaseAdapter {

        private LayoutInflater inflater;


        public WithdrawalBillAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return billArray.length();
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
            convertView = inflater.inflate(R.layout.item_yun_bu_withdrwawal_item, null);
            holder.tv_item_withdrawal_bill_title = (TextView) convertView.findViewById(R.id.tv_item_withdrawal_bill_title);
            holder.tv_item_withdrawal_bill_money = (TextView) convertView.findViewById(R.id.tv_item_withdrawal_bill_money);
            holder.tv_item_withdrawal_bill_date = (TextView) convertView.findViewById(R.id.tv_item_withdrawal_bill_date);

            holder.iv_item_withdrawal_bill_type_icon = (ImageView) convertView.findViewById(R.id.iv_item_withdrawal_bill_type_icon);

            try {
                JSONObject single = billArray.getJSONObject(position);

                if (single.getString("withdrawType").equals("支付宝")) {
                    holder.iv_item_withdrawal_bill_type_icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_alipay));
                } else {
                    holder.iv_item_withdrawal_bill_type_icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_wechat));
                }


                holder.tv_item_withdrawal_bill_title.setText(single.getString("withdrawType") + "提现");
                holder.tv_item_withdrawal_bill_money.setText("- " + Utils.getDoubleString(single.getDouble("withdrawMoney")) + "元");
                holder.tv_item_withdrawal_bill_date.setText(single.getString("withdrawDate"));


            } catch (Exception e) {
                e.printStackTrace();
            }


            return convertView;
        }


        class ViewHolder {
            TextView tv_item_withdrawal_bill_title, tv_item_withdrawal_bill_money, tv_item_withdrawal_bill_date;
            ImageView iv_item_withdrawal_bill_type_icon;
        }

    }


    public void getBillData(int pageIndex) {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("apiType", "1");
        params.put("limit", "10");
        params.put("page", String.valueOf(pageIndex));
        HttpUtils.doHttpReqeust("POST", billUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        billArray = returnJSONObject.getJSONArray("data");
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
