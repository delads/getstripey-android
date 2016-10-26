package com.delads.getstripey.com.delads.getstripey.util;

import java.util.Map;

/**
 * Created by don on 8/18/16.
 */
public class PostObject {

    private String mHost;
    private Map<String, Object> mParams;


    public void setHost(String host){
        mHost = host;
    }

    public void setParams(Map<String, Object> params){
        mParams = params;
    }

    public String getHost(){
        return mHost;
    }

    public Map<String, Object> getParams(){
        return mParams;
    }



}
