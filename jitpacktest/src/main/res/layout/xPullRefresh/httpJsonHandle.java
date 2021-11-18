
package com.lt.bubbleworld.xPullRefresh;

import org.json.JSONException;
import org.json.JSONObject;

public class httpJsonHandle {

    private JSONObject returnJSONObject;
    public JSONObject showToast(String json) throws JSONException {

        returnJSONObject=new JSONObject(json) ;

        return     returnJSONObject;

    }



}
