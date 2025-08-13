package org.fofaviewer.main;

import lombok.Setter;
import java.util.ArrayList;

public class FofaConfig {
    private static FofaConfig config = null;
    @Setter
    private boolean checkStatus;
    private String key;
//    private final String page = "1";
    public final int max = 10000;
    private String size = "1000";
    public String API = "https://fofa.info";
    public String personalInfoAPI = "https://fofa.info/api/v1/info/my?key=%s";
    // 改用分页接口
    public final String path = "/api/v1/search/all";
    public static final String TIP_API = "https://api.fofa.info/v1/search/tip?q=";
    private final String[] fields = new String[]{"host","title","ip","domain","port","protocol","server","link","country","region","city"};
    public ArrayList<String> additionalField;

    private FofaConfig(){
        this.key = "";
    }

    public static FofaConfig getInstance(){
        if (config == null) {
            config = new FofaConfig();
        }
        return config;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAPI(String API) {
        this.API = API;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size){
        this.size = size;
    }

    public String getParam(boolean isAll, int page) {
        return API + path + "?key=" + key + (isAll ? "&full=true" : "") + "&page=" + page + "&size=" + size + "&fields=" + getFields() + "&qbase64=";
    }

    public String getFields(){
        StringBuilder builder = new StringBuilder();
        for(String i : this.fields){
            builder.append(i).append(",");
        }
        for (String i : this.additionalField){
            builder.append(i).append(",");
        }
        String a = builder.toString();
        return a.substring(0,a.length()-1);
    }

    public boolean getCheckStatus() {
        return checkStatus;
    }

}
