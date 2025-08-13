
##  简介
基于`https://github.com/wgpsec/fofa_viewer`二开

##  二开说明

1、新增每页条数设置，可翻页，跳转页面。

2、首页规则展示优化，可复制语句。


<img width="1132" height="760" alt="image" src="https://github.com/user-attachments/assets/1a8b95f2-600e-42c5-bcf4-4d164bc5ee64" />


3、结果展示页单元格可复制。

<img width="1132" height="760" alt="image" src="https://github.com/user-attachments/assets/15bb3059-fe98-471a-b516-9ff3785c5901" />


4、新增证书所属组织资产查询。


<img width="1133" height="832" alt="image" src="https://github.com/user-attachments/assets/4a5dfb8d-f0c1-494c-b135-655aa63f8727" />

<img width="1132" height="760" alt="image" src="https://github.com/user-attachments/assets/03ae3d28-7853-42ec-af14-13d2865cb11c" />


5、修复ip地址排序功能，由于ipv6导致的错误。

6、右键快捷查询添加同步搜索框。


## 后期功能

❌  搜索历史

❌  界面ui优化

##  使用说明
本工具基于 FoFa 的 API 进行封装，使用时需要高级会员或者普通会员的 API key，使用注册用户的 API key 会提示账户需要充值F币。


- 如果你使用的是 JDK11 以及更高的 Java 版本，请选择不带版本号的zip包使用
- 如果你使用的是 **JDK8** 版本，请选择下载 FoFaViewer_JDK8

**导出失败问题**

JDK16+ 在导出 Excel 时会报错，可在命令行添加 JVM 参数 `--illegal-access=permit` 以导出。

JDK17+ `--illegal-access=permit`参数被移除，可在命令后添加JVM参数`--add-opens java.base/java.lang=ALL-UNNAMED`以导出。

`java -jar --add-opens java.base/java.lang=ALL-UNNAMED fofaviewer.jar  `

下载后修改 `config.properties`即可开始使用，api参数默认为`https://fofa.info`，若fofa官方更换域名可修改该参数后再使用。



