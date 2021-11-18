package com.lt.jitpacktest.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.utils.HttpUtils;
import com.lt.jitpacktest.utils.SessionSingleton;
import com.lt.jitpacktest.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class YunBuWithdrawalBindActivity extends AppCompatActivity {
    private Context context;
    private ImageView iv_withdrawal_bind_back;
    private EditText et_withdrawal_bind_real_name, et_withdrawal_bind_alipay_account;
    private TextView tv_withdrawal_bind_certain_bind, tv_withdrawal_bind_account, tv_withdrawal_bind_title;

    private RelativeLayout rl_withdrawal_bind_background;

    public Dialog mLoading;
    private String bindType, bindUrl, token, chanelUserAccount;

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

        setContentView(R.layout.activity_yun_bu_withdrawal_bind);
        context = this;

        Intent intent = getIntent();
        bindType = intent.getStringExtra("WITHDRAWALBINDTYPE");

        mLoading = Utils.createLoadingDialog(context, "正在加载......");
        mLoading.setCancelable(false);


        initview();
    }

    private void initview() {
        iv_withdrawal_bind_back = findViewById(R.id.iv_withdrawal_bind_back);
        et_withdrawal_bind_real_name = findViewById(R.id.et_withdrawal_bind_real_name);
        et_withdrawal_bind_alipay_account = findViewById(R.id.et_withdrawal_bind_alipay_account);
        tv_withdrawal_bind_certain_bind = findViewById(R.id.tv_withdrawal_bind_certain_bind);

        tv_withdrawal_bind_account = findViewById(R.id.tv_withdrawal_bind_account);
        tv_withdrawal_bind_title = findViewById(R.id.tv_withdrawal_bind_title);

        rl_withdrawal_bind_background = findViewById(R.id.rl_withdrawal_bind_background);

        bindUrl = SessionSingleton.getInstance().requestBaseUrl + "channelUserShareWithdrawApi?";

        try {
            token = SessionSingleton.getInstance().AccountSingle.getString("token");
            chanelUserAccount = SessionSingleton.getInstance().AccountSingle.getString("chanelUserAccount");

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_withdrawal_bind_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                rl_withdrawal_bind_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());

                if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                    iv_withdrawal_bind_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
                }else{
                    iv_withdrawal_bind_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        tv_withdrawal_bind_title.setText(bindType);
        if (bindType.equals("绑定支付宝")) {
            tv_withdrawal_bind_account.setText("支付宝账号");
            et_withdrawal_bind_alipay_account.setHint("请输入支付宝账号");
        } else {
            tv_withdrawal_bind_account.setText("微信账号");
            et_withdrawal_bind_alipay_account.setHint("请输入微信账号");
        }

        tv_withdrawal_bind_certain_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_withdrawal_bind_real_name.getText().toString();
                String userAccount = et_withdrawal_bind_alipay_account.getText().toString();


                if (name.equals("")) {
                    Utils.showToast(context, "请填写收款人真实姓名!");
                } else if (userAccount.length() > 0) {
                    if (bindType.equals("绑定支付宝")) {

                        if (userAccount.length() == 11) {
                            mLoading.show();
                            goBind("支付宝", userAccount, name);
                        } else if (Utils.isEmailValid(userAccount)) {
                            mLoading.show();
                            goBind("支付宝", userAccount, name);
                        } else {
                            Utils.showToast(context, "请填写正确的支付宝账号!");
                        }

                    } else {
                        if (userAccount.length() == 11) {
                            mLoading.show();
                            goBind("微信", userAccount, name);
                        } else {
                            Utils.showToast(context, "请填写正确的微信提现账号!");
                        }
                    }


                } else {
                    Utils.showToast(context, "请填写正确的提现账号!");
                }


            }
        });

        iv_withdrawal_bind_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public void goBind(String withdrawType, final String withdrawAccount, final String withdrawTrueName) {
        Map<String, String> params = new HashMap<>();
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("token", token);
        params.put("apiType", "2");
        params.put("withdrawType", withdrawType);
        params.put("withdrawAccount", withdrawAccount);
        params.put("withdrawTrueName", withdrawTrueName);
        HttpUtils.doHttpReqeust("POST",bindUrl, params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                mLoading.dismiss();
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        if (bindType.equals("绑定支付宝")) {

                            SessionSingleton.getInstance().AccountSingle.put("withdrawAalipyTrueName", withdrawTrueName);
                            SessionSingleton.getInstance().AccountSingle.put("withdrawAlipayAccount", withdrawAccount);
                        } else {

                            SessionSingleton.getInstance().AccountSingle.put("withdrawWXTrueName", withdrawTrueName);
                            SessionSingleton.getInstance().AccountSingle.put("withdrawWXAccount", withdrawAccount);
                        }

                        finish();
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

}
