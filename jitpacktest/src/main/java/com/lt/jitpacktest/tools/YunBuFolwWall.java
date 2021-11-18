package com.lt.jitpacktest.tools;

import android.content.Context;
import android.content.Intent;

import com.lt.jitpacktest.activity.YunBuNavigateActivity;
import com.lt.jitpacktest.utils.HttpUtils;
import com.lt.jitpacktest.utils.SessionSingleton;
import com.lt.jitpacktest.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class YunBuFolwWall {
    private static Context context;


    /**
     * 初始化SDK
     *
     * @param ctx
     */
    public static void init(Context ctx, String channelAccount, String chanelUserAccount, String deviceId, String oaId) {
        context = ctx;


        SessionSingleton.getInstance().ctx = ctx;
        initData(channelAccount, chanelUserAccount, deviceId, oaId);

    }

    /**
     * 初始化SDK
     *
     * @param ctx
     */
    public static void init(Context ctx, String channelAccount, String chanelUserAccount, String deviceId) {
        context = ctx;

        SessionSingleton.getInstance().ctx = ctx;
        initData(channelAccount, chanelUserAccount, deviceId, "none");

    }

    /**
     * 登录
     */
    public static void login() {
        context.startActivity(new Intent(context, YunBuNavigateActivity.class));

    }


    /**
     * initconfig
     */
    public static void initStyleConfig(YBStyleConfig YBStyleConfig) {
        SessionSingleton.getInstance().setStyleConfig(YBStyleConfig);
        SessionSingleton.getInstance().hasStyleConfig = 1;
    }

    public static void initData(String channelAccount, String chanelUserAccount, String deviceId, String oaId) {
        Map<String, String> params = new HashMap<>();
        params.put("channelAccount", channelAccount);
        params.put("chanelUserAccount", chanelUserAccount);
        params.put("deviceId", deviceId);
        params.put("oaId", oaId);
        HttpUtils.doHttpReqeust("POST", SessionSingleton.getInstance().requestBaseUrl + "channelSdkInit?", params, new HttpUtils.StringCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject returnJSONObject = new JSONObject(response);
                    if (returnJSONObject.getString("status").equals("success")) {
                        SessionSingleton.getInstance().AccountSingle = returnJSONObject.getJSONObject("userData");

                        SessionSingleton.getInstance().limitGame = returnJSONObject.getJSONObject("userData").getString("limitGame");

                        SessionSingleton.getInstance().yunbuGameMoneyScale = returnJSONObject.getJSONObject("userData").getDouble("yunbuGameMoneyScale");
                        SessionSingleton.getInstance().yunbuGameChangeScale = returnJSONObject.getJSONObject("userData").getDouble("yunbuGameChangeScale");
                        SessionSingleton.getInstance().yunbuGameGradeSerailMoneyScale = returnJSONObject.getJSONObject("userData").getDouble("yunbuGameGradeSerailMoneyScale");

                        SessionSingleton.getInstance().moneyScaleGameRecharge = returnJSONObject.getJSONObject("userData").getDouble("moneyScaleGameRecharge");
                        SessionSingleton.getInstance().moneyScale = returnJSONObject.getJSONObject("userData").getDouble("moneyScale");

                        SessionSingleton.getInstance().moneyScaleReward = returnJSONObject.getJSONObject("userData").getDouble("moneyScaleReward");

                        SessionSingleton.getInstance().yunbuXianWanMoneySpecialHandel = returnJSONObject.getJSONObject("userData").getDouble("yunbuXianWanMoneySpecialHandel");

                        if (SessionSingleton.getInstance().AccountSingle.has("noShowFinishRewardTaskId")) {
                            SessionSingleton.getInstance().noShowFinishRewardTaskId = returnJSONObject.getJSONObject("userData").getString("noShowFinishRewardTaskId");
                        } else {
                            SessionSingleton.getInstance().noShowFinishRewardTaskId = "none";
                        }
                        JSONArray array = returnJSONObject.getJSONObject("userData").getJSONArray("gameChannelSheetArray");
                        for (int i = 0; i < array.length(); i++) {
                            if (array.getJSONObject(i).getString("channelName").equals("嘻趣")) {
                                JSONObject object = array.getJSONObject(i);
                                SessionSingleton.getInstance().XiQuSingle.put("serverQQ", array.getJSONObject(i).getString("serverQQ"));
                                String[] key = array.getJSONObject(i).getString("registerKey").split("\\|");
                                SessionSingleton.getInstance().XiQuSingle.put("key", key[0]);
                                SessionSingleton.getInstance().XiQuSingle.put("secret", key[1]);
                            } else if (array.getJSONObject(i).getString("channelName").equals("聚享游")) {
                                SessionSingleton.getInstance().JuXiangWanSingle.put("serverQQ", array.getJSONObject(i).getString("serverQQ"));
                                String[] key = array.getJSONObject(i).getString("registerKey").split("\\|");
                                SessionSingleton.getInstance().JuXiangWanSingle.put("key", key[0]);
                                SessionSingleton.getInstance().JuXiangWanSingle.put("secret", key[1]);
                            } else if (array.getJSONObject(i).getString("channelName").equals("多游")) {
                                SessionSingleton.getInstance().DuoYouSingle.put("serverQQ", array.getJSONObject(i).getString("serverQQ"));
                                String[] key = array.getJSONObject(i).getString("registerKey").split("\\|");
                                SessionSingleton.getInstance().DuoYouSingle.put("key", key[0]);
                                SessionSingleton.getInstance().DuoYouSingle.put("secret", key[1]);
                            } else if (array.getJSONObject(i).getString("channelName").equals("闲玩")) {
                                SessionSingleton.getInstance().XianWanSingle.put("serverQQ", array.getJSONObject(i).getString("serverQQ"));
                                String[] key = array.getJSONObject(i).getString("registerKey").split("\\|");
                                SessionSingleton.getInstance().XianWanSingle.put("key", key[0]);
                                SessionSingleton.getInstance().XianWanSingle.put("secret", key[1]);
                            }
                        }

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
