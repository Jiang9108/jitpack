package com.lt.jitpacktest.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.lt.jitpacktest.tools.YBStyleConfig;

import org.json.JSONObject;

import java.io.File;

public class SessionSingleton {

    public static SessionSingleton instance = null;
    public String requestBaseUrl;

    public Context ctx;

    public JSONObject AccountSingle;
    public String noShowFinishRewardTaskId;

    public JSONObject rewardDetailsSingle;


    public File uploadImageFile;

    //嘻趣
    public JSONObject XiQuSingle;

    //多游
    public JSONObject DuoYouSingle;
    public String dydeviceIdsEncode = "";

    //聚享玩
    public JSONObject JuXiangWanSingle;

    //闲玩
    public JSONObject XianWanSingle;

    public double yunbuGameMoneyScale, yunbuGameChangeScale, yunbuGameGradeSerailMoneyScale, moneyScaleGameRecharge, moneyScale;

    public double moneyScaleReward;
    public double yunbuXianWanMoneySpecialHandel;

    public YBStyleConfig mYBStyleConfig = new YBStyleConfig();
    public int hasStyleConfig=0;

    public String limitGame;
    public SharedPreferences mysp;

    public static SessionSingleton getInstance() {
        if (instance == null) {
            instance = new SessionSingleton();

            instance.rewardDetailsSingle = new JSONObject();
            instance.AccountSingle = new JSONObject();

            instance.XiQuSingle = new JSONObject();
            instance.DuoYouSingle = new JSONObject();
            instance.JuXiangWanSingle = new JSONObject();
            instance.XianWanSingle = new JSONObject();


            instance.requestBaseUrl = "https://www.baiydu.com/";
            //instance.requestBaseUrl="http://192.168.1.159:8080/yulebaoServlet/";
            return instance;
        } else {
            return instance;
        }

    }


    public YBStyleConfig getStyleConfig() {
        if (this.mYBStyleConfig == null) {
            this.mYBStyleConfig = new YBStyleConfig();
        }

        return this.mYBStyleConfig;
    }

    public void setStyleConfig(YBStyleConfig YBStyleConfig) {
        this.mYBStyleConfig = YBStyleConfig;
    }
}




