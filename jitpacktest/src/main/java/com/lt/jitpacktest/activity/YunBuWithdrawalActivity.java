package com.lt.jitpacktest.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.utils.HttpUtils;
import com.lt.jitpacktest.utils.SessionSingleton;
import com.lt.jitpacktest.utils.Utils;
import com.lt.jitpacktest.xPullRefresh.XListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class YunBuWithdrawalActivity extends AppCompatActivity {
    private Context context;
    public Dialog mLoading;
    private XListView xlv_withdrawal;
    private WithdrawalAdapter adapter;

    private EditText et_withdrawal_set_money;
    private TextView tv_withdrawal_balance;

    private PopupWindow tosatPopuo;

    private int CHOOSENUMBER = 0, ISAlipayORWechat = 0, ISAllMoney = 0;

    private double balance;
    private String withdrawalUrl, refreshWithdrawalMoneyUrl, token, alipayName, alipayAccount, wechatAccount, wechatName, chanelUserAccount;

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

        setContentView(R.layout.activity_yun_bu_withdrawal);

        context = this;

        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);

        innitview();

    }

    private void innitview() {
        xlv_withdrawal = findViewById(R.id.xlv_withdrawal);

        withdrawalUrl = SessionSingleton.getInstance().requestBaseUrl + "channelUserWithdrawRquestApi?";
        refreshWithdrawalMoneyUrl = SessionSingleton.getInstance().requestBaseUrl + "channelUserFreshApi?";

        try {
            balance = SessionSingleton.getInstance().AccountSingle.getDouble("money");
            token = SessionSingleton.getInstance().AccountSingle.getString("token");
            alipayName = SessionSingleton.getInstance().AccountSingle.getString("withdrawAalipyTrueName");
            alipayAccount = SessionSingleton.getInstance().AccountSingle.getString("withdrawAlipayAccount");
            wechatAccount = SessionSingleton.getInstance().AccountSingle.getString("withdrawWXAccount");
            wechatName = SessionSingleton.getInstance().AccountSingle.getString("withdrawWXTrueName");

            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");


        } catch (JSONException e) {
            e.printStackTrace();
        }


        //listview
        xlv_withdrawal.setPullRefreshEnable(true);
        xlv_withdrawal.setPullLoadEnable(false);
        adapter = new WithdrawalAdapter(context);
        xlv_withdrawal.setXListViewListener(new XListView.IXListViewListener() {

            @Override
            public void onRefresh() {
                mLoading.show();
                RefreshMWithdrawalmoney();
                Load();
            }

            @Override
            public void onLoadMore() {

            }
        });
        xlv_withdrawal.setAdapter(adapter);
    }

    private void Load() {
        xlv_withdrawal.stopLoadMore();
        xlv_withdrawal.stopRefresh();
    }


    public class WithdrawalAdapter extends BaseAdapter {

        private LayoutInflater inflater;


        public WithdrawalAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 1;
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
            final ViewHolder holder;

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_yun_bu_withdrwawal, null);


            holder.ll_withdrawal_title_background = convertView.findViewById(R.id.ll_withdrawal_title_background);
            holder.tv_withdrawal_title_text = convertView.findViewById(R.id.tv_withdrawal_title_text);
            holder.iv_withdrawal_back = convertView.findViewById(R.id.iv_withdrawal_back);
            holder.tv_withdrawal_bill = convertView.findViewById(R.id.tv_withdrawal_bill);

            holder.tv_withdrawal_my_balance = convertView.findViewById(R.id.tv_withdrawal_my_balance);
            tv_withdrawal_balance = convertView.findViewById(R.id.tv_withdrawal_balance);

            holder.ll_withdrawal_box = convertView.findViewById(R.id.ll_withdrawal_box);

            et_withdrawal_set_money = convertView.findViewById(R.id.et_withdrawal_set_money);
            holder.tv_withdrawal_all_money = convertView.findViewById(R.id.tv_withdrawal_all_money);

            holder.ll_withdrawal_choose_pay_way = convertView.findViewById(R.id.ll_withdrawal_choose_pay_way);
            holder.ll_withdrawal_choose_pay_way_show = convertView.findViewById(R.id.ll_withdrawal_choose_pay_way_show);
            holder.iv_withdrawal_choose_pay_way_image = convertView.findViewById(R.id.iv_withdrawal_choose_pay_way_image);
            holder.tv_withdrawal_choose_pay_way_text = convertView.findViewById(R.id.tv_withdrawal_choose_pay_way_text);
            holder.iv_withdrawal_choose_pay_way_icon = convertView.findViewById(R.id.iv_withdrawal_choose_pay_way_icon);
            holder.iv_withdrawal_choose_alipay = convertView.findViewById(R.id.iv_withdrawal_choose_alipay);
            holder.iv_withdrawal_choose_wechat = convertView.findViewById(R.id.iv_withdrawal_choose_wechat);

            holder.tv_withdrawal_certain = convertView.findViewById(R.id.tv_withdrawal_certain);


            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                holder.tv_withdrawal_title_text.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                holder.ll_withdrawal_title_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());
                tv_withdrawal_balance.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                holder.tv_withdrawal_my_balance.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());

                if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                    holder.iv_withdrawal_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                }else{
                    holder.iv_withdrawal_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }

            try {
                tv_withdrawal_balance.setText(balance + "");

                holder.tv_withdrawal_all_money.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ISAllMoney = 1;
                        et_withdrawal_set_money.setText(balance + "");
                    }
                });


                holder.iv_withdrawal_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                holder.tv_withdrawal_bill.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(YunBuWithdrawalActivity.this, YunBuWithdrawalBillActivity.class);
                        startActivity(intent);
                    }
                });

                holder.tv_withdrawal_certain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double money = 0;

                        if (ISAllMoney == 0) {
                            if (et_withdrawal_set_money.getText().toString().equals("")) {
                                money = 0;
                            } else {
                                money = Double.valueOf(et_withdrawal_set_money.getText().toString());
                            }

                        } else {
                            money = balance;
                        }

                        if (money >= 1) {
                            if (money <= balance) {
                                if (ISAlipayORWechat == 0) {
                                    if (alipayAccount.equals("none")) {

                                        showToast("提现需绑定支付宝账户！", 1);
                                    } else {
                                        Withdrawal("支付宝", alipayAccount, alipayName, String.valueOf(money));
                                    }

                                } else {
                                    if (wechatAccount.equals("none")) {
                                        showToast("提现需绑定微信账户！", 2);
                                    } else {
                                        Withdrawal("微信", wechatAccount, wechatName, String.valueOf(money));
                                    }
                                }
                            } else {
                                Utils.showToast(context, "您的余额不足！");
                            }

                        } else {
                            Utils.showToast(context, "最低提现金额不得小于1元！");
                        }


                    }
                });

                holder.ll_withdrawal_choose_pay_way.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View v) {
                        if (CHOOSENUMBER == 0) {
                            CHOOSENUMBER = 1;
                            holder.ll_withdrawal_choose_pay_way_show.setVisibility(View.VISIBLE);
                            holder.ll_withdrawal_box.setBackground(getResources().getDrawable(R.mipmap.bg_yunbu_withdrawal_box_big));
                        } else {
                            CHOOSENUMBER = 0;
                            holder.ll_withdrawal_choose_pay_way_show.setVisibility(View.GONE);
                            holder.ll_withdrawal_box.setBackground(getResources().getDrawable(R.mipmap.bg_yunbu_withdrawal_box_small));
                        }

                    }
                });
                holder.iv_withdrawal_choose_alipay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ISAlipayORWechat = 0;
                        holder.iv_withdrawal_choose_alipay.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_tag));
                        holder.iv_withdrawal_choose_wechat.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_untag));

                        holder.iv_withdrawal_choose_pay_way_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_alipay));
                        holder.tv_withdrawal_choose_pay_way_text.setText("支付宝");
                    }
                });

                holder.iv_withdrawal_choose_wechat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ISAlipayORWechat = 1;
                        holder.iv_withdrawal_choose_alipay.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_untag));
                        holder.iv_withdrawal_choose_wechat.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_tag));

                        holder.iv_withdrawal_choose_pay_way_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_withdrawal_wechat));
                        holder.tv_withdrawal_choose_pay_way_text.setText("微信");
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }


            return convertView;
        }


        class ViewHolder {
            TextView tv_withdrawal_all_money, tv_withdrawal_choose_pay_way_text, tv_withdrawal_certain;
            ImageView iv_withdrawal_choose_pay_way_image, iv_withdrawal_choose_pay_way_icon, iv_withdrawal_choose_alipay, iv_withdrawal_choose_wechat;

            LinearLayout ll_withdrawal_box, ll_withdrawal_choose_pay_way, ll_withdrawal_choose_pay_way_show;

            LinearLayout ll_withdrawal_title_background;
            ImageView iv_withdrawal_back;
            TextView tv_withdrawal_bill, tv_withdrawal_title_text,tv_withdrawal_my_balance;
        }

    }

    public void RefreshMWithdrawalmoney() {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        HttpUtils.doHttpReqeust("POST", refreshWithdrawalMoneyUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {

                        balance = returnJSONObject.getDouble("money");

                        tv_withdrawal_balance.setText(balance + "");
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
                mLoading.dismiss();
            }
        });

    }

    public void Withdrawal(String withdrawType, String withdrawAccount, String withdrawTrueName, final String withdrawMoney) {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("withdrawType", withdrawType);
        params.put("withdrawAccount", withdrawAccount);
        params.put("withdrawTrueName", withdrawTrueName);
        params.put("withdrawMoney", withdrawMoney);
        HttpUtils.doHttpReqeust("POST", withdrawalUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {

                        double money = SessionSingleton.getInstance().AccountSingle.getDouble("money") - Double.valueOf(withdrawMoney);
                        SessionSingleton.getInstance().AccountSingle.put("money", money);

                        showToast(returnJSONObject.getString("msg"), 0);

                        tv_withdrawal_balance.setText(Utils.getDoubleString(money));
                        balance = money;
                        et_withdrawal_set_money.setText("");
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

    //提示
    private void showToast(final String message, final int type) {

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
                if (type == 0) {
                    tosatPopuo.dismiss();
                } else if (type == 1) {
                    tosatPopuo.dismiss();
                    Intent intent = new Intent(YunBuWithdrawalActivity.this, YunBuWithdrawalBindActivity.class);
                    intent.putExtra("WITHDRAWALBINDTYPE", "绑定支付宝");
                    startActivity(intent);
                } else if (type == 2) {
                    tosatPopuo.dismiss();
                    Intent intent = new Intent(YunBuWithdrawalActivity.this, YunBuWithdrawalBindActivity.class);
                    intent.putExtra("WITHDRAWALBINDTYPE", "绑定微信");
                    startActivity(intent);
                } else {
                    tosatPopuo.dismiss();
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


    @Override
    protected void onResume() {
        super.onResume();
        try {
            balance = SessionSingleton.getInstance().AccountSingle.getDouble("money");
            alipayName = SessionSingleton.getInstance().AccountSingle.getString("withdrawAalipyTrueName");
            alipayAccount = SessionSingleton.getInstance().AccountSingle.getString("withdrawAlipayAccount");
            wechatAccount = SessionSingleton.getInstance().AccountSingle.getString("withdrawWXAccount");
            wechatName = SessionSingleton.getInstance().AccountSingle.getString("withdrawWXTrueName");

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
