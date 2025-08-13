package org.fofaviewer.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class TableBean extends BaseBean{
    public int num;
    public String host;
    public String title;
    public String ip;
    public String domain;
    public int port;
    public String protocol;
    public String server;
    public String lastupdatetime;
    public String fid;
    public String os;
    public String icp;
    public String product;
    public String certCN;
    public String certOrg;
    public String status;
    public String country;
    public String region;
    public String city;

    public TableBean(int num, String host, String title, String ip, String domain, int port, String protocol, String server) {
        this.num = num;
        this.host = host;
        this.title = title;
        this.ip = ip;
        this.domain = domain;
        this.port = port;
        this.protocol = protocol;
        this.server = server;
    }

    public int getIntNum(){
        return num;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TableBean))
            return false;
        if (this == obj)
            return true;
        TableBean instance = (TableBean) obj;
        boolean bool_host = this.host.equals(instance.host);
        boolean bool_port = this.port == instance.port;
        if(bool_port){
            if(this.port == 443 && (this.host.contains(":443") || instance.host.contains(":443"))){
                bool_host = true;
            }
            if(this.port == 80 && (this.host.contains(":80") || instance.host.contains(":80"))){
                bool_host = true;
            }
        }
        boolean bool_ip = this.ip.equals(instance.ip);
        return bool_host && bool_ip && bool_port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}