package org.fofaviewer.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.controlsfx.control.StatusBar;
import org.controlsfx.dialog.CommandLinksDialog;
import org.controlsfx.dialog.ProgressDialog;
import org.fofaviewer.bean.*;
import org.fofaviewer.callback.SaveOptionCallback;
import org.fofaviewer.controls.*;
import org.fofaviewer.main.FofaConfig;
import org.fofaviewer.callback.MainControllerCallback;
import org.fofaviewer.request.Request;
import org.fofaviewer.callback.RequestCallback;
import org.fofaviewer.utils.DataUtil;
import org.fofaviewer.utils.RequestUtil;
import org.controlsfx.control.textfield.TextFields;
import org.fofaviewer.utils.ResourceBundleUtil;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import org.fofaviewer.utils.SQLiteUtils;
import org.tinylog.Logger;

public class MainController {
    private Map<String, Object> projectInfo;
    
    private static final RequestUtil helper = RequestUtil.getInstance();
    private FofaConfig client;
    private final ResourceBundle resourceBundle;
    private final HashMap<CheckBox, String> keyMap = new HashMap<>();
    @FXML
    private Menu help;
    @FXML
    private Menu project;
    @FXML
    private Menu rule;
    @FXML
    private Menu config;
    @FXML
    private MenuItem query_api;
    @FXML
    private MenuItem createRule;
    @FXML
    private MenuItem exportRule;
    @FXML
    private MenuItem about;
    @FXML
    private MenuItem setConfig;
    @FXML
    private MenuItem openProject;
    @FXML
    private MenuItem saveProject;
    @FXML
    private Button exportDataBtn;
    @FXML
    private Button searchBtn;
    @FXML
    private Label queryString;
    @FXML
    private VBox rootLayout;
    @FXML
    private TextField queryTF;
    @FXML
    private CheckBox checkHoneyPot;
    @FXML
    private CheckBox withFid;
    @FXML
    private CheckBox os;
    @FXML
    private CheckBox icp;
    @FXML
    private CheckBox product;
    @FXML
    private CheckBox certs_subject_cn;
    @FXML
    private CheckBox certs_subject_org;
    @FXML
    private CheckBox lastUpdateTime;
    @FXML
    private CheckBox isAll;
    @FXML
    private CloseableTabPane tabPane;
    @FXML
    private ComboBox<Integer> sizeComboBox;
    @FXML
    private Button prevBtn;
    @FXML
    private Label pageLabel;
    @FXML
    private Button nextBtn;
    @FXML
    private TextField pageJumpTF;
    @FXML
    private Button jumpBtn;


    public MainController(){
        this.resourceBundle = ResourceBundleUtil.getResource();
    }

    @FXML
    private void initialize() {
        SQLiteUtils.init();
        keyMap.put(withFid, "fid");
        keyMap.put(os, "os");
        keyMap.put(icp, "icp");
        keyMap.put(product, "product");
        keyMap.put(certs_subject_cn, "certs_subject_cn");
        keyMap.put(certs_subject_org,"certs_subject_org");
        keyMap.put(lastUpdateTime, "lastupdatetime");
        projectInfo = new HashMap<>();
        projectInfo.put("status", Boolean.FALSE);
        projectInfo.put("name", "");
        withFid.setText(resourceBundle.getString("WITH_FID"));
        certs_subject_cn.setText(resourceBundle.getString("CERT_CN"));
        os.setText("os");
        icp.setText("icp");
        product.setText(resourceBundle.getString("PRODUCT_FINGER"));
        certs_subject_org.setText(resourceBundle.getString("CERT_ORG"));
        lastUpdateTime.setText(resourceBundle.getString("LAST_UPDATE_TIME"));
        about.setText(resourceBundle.getString("ABOUT"));
        help.setText(resourceBundle.getString("HELP"));
        project.setText(resourceBundle.getString("PROJECT"));
        config.setText(resourceBundle.getString("CONFIG_PANEL"));
        rule.setText(resourceBundle.getString("RULE"));
        query_api.setText(resourceBundle.getString("QUERY_API"));
        setConfig.setText(resourceBundle.getString("SET_CONFIG"));
        createRule.setText(resourceBundle.getString("CREATE_RULE"));
        exportRule.setText(resourceBundle.getString("EXPORT_RULE"));
        saveProject.setText(resourceBundle.getString("SAVE_PROJECT"));
        openProject.setText(resourceBundle.getString("OPEN_PROJECT"));
        searchBtn.setText(resourceBundle.getString("SEARCH"));
        exportDataBtn.setText(resourceBundle.getString("EXPORT_BUTTON"));
        queryString.setText(resourceBundle.getString("QUERY_CONTENT"));
        isAll.setText(resourceBundle.getString("IS_ALL"));
        checkHoneyPot.setText(resourceBundle.getString("REMOVE_HONEYPOTS"));
        
        this.client = DataUtil.loadConfigure();
        this.tabPane.setCallback(new MainControllerCallback() {
            @Override
            public void queryCall(List<String> strList) {
                query(strList);
            }
        });

        // Enter a search
        queryTF.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                queryAction();
            }
        });

        // Initialize paging controls
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
        jumpBtn.setDisable(true);
        pageJumpTF.setDisable(true);
        prevBtn.setOnAction(event -> changePage(false));
        nextBtn.setOnAction(event -> changePage(true));
        jumpBtn.setOnAction(event -> {
            String text = pageJumpTF.getText();
            if (text != null && !text.isEmpty()) {
                try {
                    int page = Integer.parseInt(text);
                    loadPage(page);
                } catch (NumberFormatException e) {
                    DataUtil.showAlert(Alert.AlertType.ERROR, null, "请输入有效的页码").showAndWait();
                }
            }
        });

        // Initialize page size selector
        sizeComboBox.getItems().addAll(100, 500, 1000, 2000);
        sizeComboBox.setValue(Integer.parseInt(client.getSize()));
        sizeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != oldVal) {
                client.setSize(String.valueOf(newVal));
                Tab currentTab = tabPane.getCurrentTab();
                if (currentTab != null && !currentTab.getText().equals(resourceBundle.getString("HOMEPAGE"))) {
                    loadPage(1); // Refresh current tab with new size
                }
            }
        });

        tabPane.getTabPane().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                updatePagingControl();
            }
        });

        // Initialize home page tab
        Tab tab = this.tabPane.getTab(resourceBundle.getString("HOMEPAGE"));
        Button queryCert = new Button(resourceBundle.getString("QUERY_BUTTON"));
        Button queryFavicon = new Button(resourceBundle.getString(("QUERY_BUTTON")));
        Label label = new Label(resourceBundle.getString("CERT_LABEL"));
        Label faviconLabel = new Label(resourceBundle.getString("FAVICON_LABEL"));
        TextField tf = TextFields.createClearableTextField();
        TextField favionTF = TextFields.createClearableTextField();
        favionTF.setOnDragOver(event -> {
            if (event.getGestureSource() != favionTF){
                event.acceptTransferModes(TransferMode.ANY);
            }
        });
        favionTF.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()){
                try {
                    File file = dragboard.getFiles().get(0);
                    if (file != null && file.exists()) {
                        favionTF.setText(file.getAbsolutePath());
                    }
                }catch (Exception e){
                    Logger.error(e.toString());
                }
            }
        });

        TableView<RuleBean> tableView = new TableView<>();
        tableView.setEditable(true);
        TableColumn<RuleBean, String> syntaxCol = new TableColumn<>("查询语法");
        TableColumn<RuleBean, String> descCol = new TableColumn<>("说明");
        TableColumn<RuleBean, String> remarkCol = new TableColumn<>("备注");

        syntaxCol.setCellValueFactory(new PropertyValueFactory<>("syntax"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        remarkCol.setCellValueFactory(new PropertyValueFactory<>("remark"));

        syntaxCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        remarkCol.setCellFactory(TextFieldTableCell.forTableColumn());

        tableView.getColumns().addAll(syntaxCol, descCol, remarkCol);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final KeyCodeCombination keyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        tableView.setOnKeyPressed(event -> {
            if (keyCodeCombination.match(event)) {
                copySelectionToClipboard(tableView);
            }
        });

        String[] syntaxData = {
                "headerName=\"beijing\"", "从标题中搜索“北京”", "",
                "header=\"elastic\"", "从http头中搜索“elastic”", "",
                "body=\"网络空间测绘\"", "从html正文中搜索“网络空间测绘”", "",
                "fid=\"sSXXGNUO2FefBTcCLIT/2Q==\"", "查找相同的网站指纹", "搜索网站类型资产",
                "domain=\"qq.com\"", "搜索根域名带有qq.com的网站", "",
                "icp=\"京ICP证030173号\"", "查找备案号为“京ICP证030173号”的网站", "搜索网站类型资产",
                "js_name=\"js/jquery.js\"", "查找网站正文中包含js/jquery.js的资产", "搜索网站类型资产",
                "js_md5=\"82ac3f14327a8b7ba49baa208d4eaa15\"", "查找js源码与之匹配的资产", "",
                "cname=\"ap21.inst.siteforce.com\"", "查找cname为\"ap21.inst.siteforce.com\"的网站", "",
                "cname_domain=\"siteforce.com\"", "查找cname包含“siteforce.com”的网站", "",
                "cloud_name=\"Aliyundun\"", "通过云服务名称搜索资产", "",
                "product=\"NGINX\"", "搜索此产品的资产", "个人版及以上可用",
                "category=\"服务\"", "搜索此产品分类的资产", "个人版及以上可用",
                "sdk_hash==\"Mkb4Ms4R96glv/T6TRzwPWh3UDatBqeF\"", "搜索使用此sdk的资产", "商业版及以上可用",
                "icon_hash=\"-247388890\"", "搜索使用此 icon 的资产", "",
                "host=\".gov.cn\"", "从url中搜索”.gov.cn”", "搜索要用host作为名称",
                "port=\"6379\"", "查找对应“6379”端口的资产", "",
                "ip=\"1.1.1.1\"", "从ip中搜索包含“1.1.1.1”的网站", "搜索要用ip作为名称",
                "ip=\"220.181.111.1/24\"", "查询IP为“220.181.111.1”的C网段资产", "",
                "status_code=\"402\"", "查询服务器状态为“402”的资产", "查询网站类型数据",
                "protocol=\"quic\"", "查询quic协议资产", "搜索指定协议类型(在开启端口扫描的情况下有效)",
                "country=\"CN\"", "搜索指定国家(编码)的资产", "",
                "region=\"Xinjiang Uyghur Autonomous Region\"", "搜索指定行政区的资产", "",
                "city=\"Ürümqi\"", "搜索指定城市的资产", "",
                "cert=\"baidu\"", "搜索证书(https或者imaps等)中带有baidu的资产", "",
                "cert.subject=\"Oracle Corporation\"", "搜索证书持有者是Oracle Corporation的资产", "",
                "cert.issuer=\"DigiCert\"", "搜索证书颁发者为DigiCert Inc的资产", "",
                "cert.is_valid=true", "验证证书是否有效，true有效，false无效", "个人版及以上可用",
                "cert.is_match=true", "证书和域名是否匹配；true匹配、false不匹配", "个人版及以上可用",
                "cert.is_expired=false", "证书是否过期；true过期、false未过期", "个人版及以上可用",
                "jarm=\"2ad...83e81\"", "搜索JARM指纹", "",
                "banner=\"users\" && protocol=\"ftp\"", "搜索FTP协议中带有users文本的资产", "",
                "type=\"service\"", "搜索所有协议资产，支持subdomain和service两种", "搜索所有协议资产",
                "os=\"centos\"", "搜索CentOS资产", "",
                "server==\"Microsoft-IIS/10\"", "搜索IIS 10服务器", "",
                "app=\"Microsoft-Exchange\"", "搜索Microsoft-Exchange设备", "",
                "after=\"2017\" && before=\"2017-10-01\"", "时间范围段搜索", "",
                "asn=\"19551\"", "搜索指定asn的资产", "",
                "org=\"LLC Baxet\"", "搜索指定org(组织)的资产", "",
                "base_protocol=\"udp\"", "搜索指定udp协议的资产", "",
                "is_fraud=false", "排除仿冒/欺诈数据", "专业版及以上可用",
                "is_honeypot=false", "排除蜜罐数据", "专业版及以上可用",
                "is_ipv6=true", "搜索ipv6的资产", "搜索ipv6的资产,只接受true和false",
                "is_domain=true", "搜索域名的资产", "搜索域名的资产,只接受true和false",
                "is_cloud=true", "筛选使用了云服务的资产", "",
                "port_size=\"6\"", "查询开放端口数量等于\"6\"的资产", "个人版及以上可用",
                "port_size_gt=\"6\"", "查询开放端口数量大于\"6\"的资产", "个人版及以上可用",
                "port_size_lt=\"12\"", "查询开放端口数量小于\"12\"的资产", "个人版及以上可用",
                "ip_ports=\"80,161\"", "搜索同时开放80和161端口的ip", "搜索同时开放80和161端口的ip资产(以ip为单位的资产数据)",
                "ip_country=\"CN\"", "搜索中国的ip资产(以ip为单位的资产数据)", "搜索中国的ip资产",
                "ip_region=\"Zhejiang\"", "搜索指定行政区的ip资产(以ip为单位的资产数据)", "搜索指定行政区的资产",
                "ip_city=\"Hangzhou\"", "搜索指定城市的ip资产(以ip为单位的资产数据)", "搜索指定城市的资产",
                "ip_after=\"2021-03-18\"", "搜索2021-03-18以后的ip资产(以ip为单位的资产数据)", "搜索2021-03-18以后的ip资产",
                "ip_before=\"2019-09-09\"", "搜索2019-09-09以前的ip资产(以ip为单位的资产数据)", "搜索2019-09-09以前的ip资产"
        };

        for (int i = 0; i < syntaxData.length; i += 3) {
            tableView.getItems().add(new RuleBean(syntaxData[i], syntaxData[i+1], syntaxData[i+2]));
        }

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        tf.setPromptText(resourceBundle.getString("CERT_HINT"));
        favionTF.setPromptText(resourceBundle.getString("FAVICON_HINT"));
        tf.setPrefWidth(400);
        favionTF.setPrefWidth(400);
        label.setFont(Font.font(14));
        faviconLabel.setFont(Font.font(14));
        queryCert.setOnAction(event -> {
            String txt = tf.getText().trim();
            if(!txt.isEmpty()){
                String serialnumber = txt.replaceAll(" ", "");
                BigInteger i = new BigInteger(serialnumber, 16);
                query(new ArrayList<String>(){{add("cert=\"" + i + "\"");}});
            }
        });
        queryFavicon.setOnAction(event -> {
            String text = favionTF.getText().trim();
            if(!text.isEmpty()){
                if(!text.startsWith("http")){ // Import files
                    String suffix = text.substring(text.lastIndexOf(".")+1).toLowerCase();
                    try {
                        byte[] content = Files.readAllBytes(Paths.get(text));
                        switch (suffix){
                            case "jpg": case "png": case "ico": case "svg":
                                String encode = java.util.Base64.getMimeEncoder().encodeToString(content);
                                query(new ArrayList<String>(){{add("icon_hash=\"" + helper.getIconHash(encode) + "\"");}});
                                break;
                            default:
                                DataUtil.showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("ERROR_FILE")).showAndWait();
                                break;
                        }
                    } catch (IOException e) {
                        DataUtil.showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("ERROR_FILE")).showAndWait();
                        Logger.error(e);
                    }
                }else { // url import
                    HashMap<String,String> res = helper.getImageFavicon(text);
                    if(res != null){
                        if(res.get("code").equals("error")){
                            DataUtil.showAlert(Alert.AlertType.ERROR, null, res.get("msg")).showAndWait();return;
                        }
                        query(new ArrayList<String>(){{add(res.get("msg"));}});
                    }
                }
            }
        });
        VBox vb = new VBox();
        HBox hb = new HBox();
        HBox faviconBox = new HBox();
        vb.setSpacing(10);
        hb.getChildren().addAll(label, tf, queryCert);
        label.setPadding(new Insets(3));
        faviconLabel.setPadding(new Insets(3,5,3,3));
        hb.setPadding(new Insets(10));
        hb.setSpacing(15);
        hb.setAlignment(Pos.TOP_CENTER);
        faviconBox.setAlignment(Pos.TOP_CENTER);
        faviconBox.setPadding(new Insets(5,0,5,0));
        faviconBox.getChildren().addAll(faviconLabel, favionTF, queryFavicon);
        vb.getChildren().addAll(hb, faviconBox, scrollPane);
        tab.setContent(vb);
    }

    private void copySelectionToClipboard(TableView<RuleBean> tableView) {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = tableView.getSelectionModel().getSelectedCells();
        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();
            Object cell = tableView.getColumns().get(col).getCellData(row);
            if (cell == null) {
                cell = "";
            }
            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }
            clipboardString.append(cell);
            prevRow = row;
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    public static class RuleBean {
        private final String syntax;
        private final String description;
        private final String remark;

        public RuleBean(String syntax, String description, String remark) {
            this.syntax = syntax;
            this.description = description;
            this.remark = remark;
        }

        public String getSyntax() {
            return syntax;
        }

        public String getDescription() {
            return description;
        }

        public String getRemark() {
            return remark;
        }
    }

    public void openFile(String fileName){
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            List<String> list = new ArrayList<>();
            String str;
            while((str = bufferedReader.readLine()) != null) {
                if(!str.isEmpty()){
                    list.add(str);
                }
            }
            query(list);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    @FXML
    private void getQueryAPI(){
        Tab tab = this.tabPane.getCurrentTab();
        if(tab.getText().equals(resourceBundle.getString("HOMEPAGE"))){
            DataUtil.showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("COPY_QUERY_URL_FAILED")).showAndWait();
        }else{
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(this.tabPane.getCurrentQuery(tab));
            clipboard.setContent(content);
            DataUtil.showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("COPY_QUERY_URL_SUCCESS")).showAndWait();
        }
    }

    @FXML
    private void queryAction(){
        if(queryTF.getText() != null && !queryTF.getText().trim().isEmpty()){
            query(new ArrayList<String>(){{add(queryTF.getText());}});
        }
    }

    @FXML
    private void showAbout(){
        List<CommandLinksDialog.CommandLinksButtonType> clb = Arrays.asList(
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer",
                        resourceBundle.getString("ABOUT_HINT1"), true),
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer/issues",
                        resourceBundle.getString("ABOUT_HINT2"), true)
        );
        CommandLinksDialog dialog = new CommandLinksDialog(clb);
        dialog.setOnCloseRequest(e -> {
            ButtonType result = dialog.getResult();
            if(result.getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE){
                URI uri = URI.create(result.getText());
                Desktop dp = Desktop.getDesktop();
                if (dp.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        dp.browse(uri);
                    } catch (IOException ex) {
                        Logger.error(ex);
                    }
                }
            }
        });
        dialog.setTitle("Notice");
        dialog.setContentText("WgpSec Team");
        dialog.showAndWait();
    }

    @FXML
    private void setConfig(){
        SetConfiDialog dialog = new SetConfiDialog(resourceBundle.getString("CONFIG_PANEL"));
        dialog.showAndWait();
    }

    @FXML
    private void openProject(){
        if((Boolean) projectInfo.get("status")){
            Alert dialog = DataUtil.showAlert(Alert.AlertType.CONFIRMATION, null, resourceBundle.getString("OPEN_NEW_PROCESS"));
            dialog.setOnCloseRequest(event -> {
                ButtonType btn = dialog.getResult();
                if(btn.equals(ButtonType.OK)){
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle(resourceBundle.getString("FILE_CHOOSER_TITLE"));
                    File file = chooser.showOpenDialog(rootLayout.getScene().getWindow());
                    if(file != null){
                        String os = System.getProperty("os.name").toLowerCase();
                        String javaPath = System.getProperty("java.home") + FileSystems.getDefault().getSeparator() + "bin" + FileSystems.getDefault().getSeparator();
                        try {
                            if(os.contains("windows")){
                                String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);
                                javaPath += "java.exe";
                                Runtime.getRuntime().exec(new String[]{"cmd", "/c", javaPath, "-jar", jarPath, "-f", file.getAbsolutePath()});
                            }else{
                                String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
                                System.out.println(jarPath);
                                javaPath += "java";
                                Runtime.getRuntime().exec(new String[]{"sh", "-c", "\"" + javaPath, "-jar", jarPath, "-f", file.getAbsolutePath(), "\""});
                            }
                        }catch (IOException e) {
                            Logger.error(e);
                        }
                    }
                }
            });
            dialog.showAndWait();
        }else{
            FileChooser chooser = new FileChooser();
            chooser.setTitle(resourceBundle.getString("FILE_CHOOSER_TITLE"));
            File file = chooser.showOpenDialog(rootLayout.getScene().getWindow());
            if(file != null){
                openFile(file.getAbsolutePath());
            }
        }
    }

    @FXML
    private void saveProject(){
        if(this.tabPane.getTabs().size() == 1){
            DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("SAVE_PROJECT_ERROR")).showAndWait();
        }else{
            SaveOptionCallback callback = new SaveOptionCallback() {
                @Override
                public void setProjectName(String name) {
                    projectInfo.put("name", name);
                }
                @Override
                public String getProjectName() {
                    return projectInfo.get("name").toString();
                }
            };
            SaveOptionDialog sd = new SaveOptionDialog(this.tabPane, true, callback);
            sd.setOnCloseRequest(event -> {
                ButtonType rs = sd.getResult();
                if(rs.equals(ButtonType.OK)){
                    projectInfo.put("status", Boolean.TRUE);
                }
            });
            sd.showAndWait();
        }
    }

    @FXML
    private void createRule(){
        if(this.tabPane.getTabs().size() == 1){
            DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("SAVE_RULE_ERROR")).showAndWait();
        }else{
            SaveOptionDialog sd = new SaveOptionDialog(this.tabPane, false, null);
            sd.showAndWait();
        }
    }

    @FXML
    private void exportRule(){

    }

    @FXML
    private void exportAction() {
        Tab tab = tabPane.getCurrentTab();
        if(tab.getText().equals(resourceBundle.getString("HOMEPAGE"))) return;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resourceBundle.getString("DIRECTORY_CHOOSER_TITLE"));
        File file = directoryChooser.showDialog(rootLayout.getScene().getWindow());
        if(file != null){
            TabDataBean bean = this.tabPane.getTabDataBean(tab);
            HashSet<String> urlList = new HashSet<>();
            List<ExcelBean> totalData = new ArrayList<>();
            StringBuilder errorPage = new StringBuilder();
            int maxCount = bean.total;
            int pageSize = sizeComboBox.getValue();
            int totalPage = (int)Math.ceil(maxCount / (double)pageSize);

            TextInputDialog td = new TextInputDialog();
            td.setTitle(resourceBundle.getString("EXPORT_CONFIRM"));
            td.setHeaderText("共 " + bean.total + " 条数据，将分 " + totalPage + " 页导出。");
            td.setContentText("请输入要导出的总页数 (默认全部):");
            Optional<String> result = td.showAndWait();

            if (!result.isPresent()) { // User clicked cancel
                return;
            }

            int inputPage = totalPage;
            if (!result.get().isEmpty()){ // User entered a value
                try{
                    inputPage = Integer.parseInt(result.get());
                    if(inputPage <= 0 || inputPage > totalPage){
                        DataUtil.showAlert(Alert.AlertType.ERROR, null, "页数必须在 1 到 " + totalPage + " 之间").showAndWait();
                        return;
                    }
                }catch (NumberFormatException ex){
                    DataUtil.showAlert(Alert.AlertType.ERROR,null, resourceBundle.getString("EXPORT_INPUT_NUM_ERROR")).showAndWait();
                    return;
                }
            }
            int finalTotalPage = inputPage;
            Task<Void> exportTask = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        for (int i = 1; i <= finalTotalPage; i++) {
                            Thread.sleep(300);
                            String text = DataUtil.replaceString(tab.getText());
                            String url = client.getParam(isAll.isSelected(), i) + helper.encode(text);
                            HashMap<String, String> result = helper.getHTML(url, 50000, 50000);
                            if (result.get("code").equals("200")) {
                                JSONObject obj = JSON.parseObject(result.get("msg"));
                                DataUtil.loadJsonData(null, obj, totalData, urlList, true);
                                updateMessage("正在加载: " + i + "/" + finalTotalPage + " 页");
                                updateProgress(i, finalTotalPage);
                            } else {
                                errorPage.append(i).append(" ");
                            }
                        }
                    }catch (Exception e){
                        Logger.error(e);
                    }
                    return null;
                }
            };
            ProgressDialog pd = new ProgressDialog(exportTask);
            pd.setTitle(resourceBundle.getString("EXPORT_TITLE"));
            pd.setHeaderText("正在导出 " + (inputPage * pageSize) + " 条数据，共 " + finalTotalPage + " 页");
            new Thread(exportTask).start();
            pd.showAndWait();

            List<List<String>> urls = new ArrayList<>();
            for(String i : urlList){
                List<String> item = new ArrayList<>();
                item.add(i);
                urls.add(item);
            }
            String fileName = file.getAbsolutePath() + FileSystems.getDefault().getSeparator() 
                    + resourceBundle.getString("EXPORT_FILENAME") + System.currentTimeMillis() + ".xlsx";
            DataUtil.exportToExcel(fileName, tab.getText(), totalData, urls, errorPage);
        }
    }

    public void query(List<String> strList){
        if (strList != null && !strList.isEmpty()) {
            queryTF.setText(strList.get(0));
        }
        ArrayList<String> additionalField = new ArrayList<>();
        for (CheckBox box : keyMap.keySet()) {
            if (box.isSelected()) {
                additionalField.add(keyMap.get(box));
            }
        }
        client.additionalField = additionalField;
        ArrayList<RequestBean> beans = new ArrayList<>();
        for(String text:strList) {
            String tabTitle = text.trim();
            if (text.startsWith("(*)")) {
                tabTitle = text;
                text = text.substring(3);
                text = "(" + text + ") && (is_honeypot=false && is_fraud=false)";
            }
            if (checkHoneyPot.isSelected() && !text.contains("(is_honeypot=false && is_fraud=false)")) {
                tabTitle = "(*)" + text;
                text = "(" + text + ") && (is_honeypot=false && is_fraud=false)";
            }
            final String queryText = text;
            if (this.tabPane.isExistTab(tabTitle)) {
                Tab existTab = this.tabPane.getTab(tabTitle);
                this.tabPane.setCurrentTab(existTab);
                loadPage(1); // Force refresh
                continue;
            }
            Tab tab = new Tab();
            tab.setOnCloseRequest(event -> tabPane.closeTab(tab));
            tab.setText(tabTitle);
            tab.setTooltip(new Tooltip(tabTitle));
            String url = client.getParam(isAll.isSelected(), 1) + helper.encode(queryText);
            RequestBean bean = new RequestBean(url, tabTitle, client.getSize());
            bean.setTab(tab);
            beans.add(bean);
        }
        MainControllerCallback mCallback = new MainControllerCallback() {
            @Override
            public void queryCall(List<String> strList) {
                query(strList);
            }
        };

        new Request(beans, new RequestCallback<Request>() {
            @Override
            public void before(TabDataBean tabDataBean, RequestBean bean) {
                tabPane.addTab(bean.getTab(), tabDataBean, bean.getRequestUrl());
                tabPane.setCurrentTab(bean.getTab());
                LoadingPane ld = new LoadingPane();
                bean.getTab().setContent(ld);
            }

            @Override
            public void succeeded(BorderPane tablePane, StatusBar bar, RequestBean bean) {
                if (bean.getResult().get("code").equals("200")) {
                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(tablePane);
                    bean.getTab().setContent(stackPane);
                    
                    tabPane.addBar(bean.getTab(), bar);
                    updatePagingControl();
                } else {
                    ((LoadingPane)bean.getTab().getContent()).setErrorText("请求状态码："+bean.getResult().get("code")+ bean.getResult().get("msg"));
                }
            }

            @Override
            public void failed(String text, RequestBean bean) {
                Tab tab = bean.getTab();
                if (tab != null && tab.getContent() instanceof LoadingPane) {
                    String errorMessage = text;
                    if (errorMessage == null || errorMessage.trim().isEmpty()) {
                        errorMessage = "An unknown error occurred.";
                    }
                    ((LoadingPane) tab.getContent()).setErrorText(errorMessage);
                }
            }
        }, mCallback).query(beans.size());
    }

    private void changePage(boolean isNext) {
        Tab currentTab = tabPane.getCurrentTab();
        if (currentTab == null) return;
        TabDataBean bean = tabPane.getTabDataBean(currentTab);
        if (bean == null) return;
        int newPage = bean.page + (isNext ? 1 : -1);
        loadPage(newPage);
    }

    private void loadPage(int page) {
        Tab currentTab = tabPane.getCurrentTab();
        if (currentTab == null || currentTab.getText().equals(resourceBundle.getString("HOMEPAGE"))) {
            return;
        }
        TabDataBean bean = tabPane.getTabDataBean(currentTab);
        if (bean == null) return;

        if (!(currentTab.getContent() instanceof StackPane)) {
            // Content is not a StackPane, likely a LoadingPane. Do nothing.
            return;
        }
        StackPane stackPane = (StackPane) currentTab.getContent();

        LoadingOverlay overlay = new LoadingOverlay();
        stackPane.getChildren().add(overlay);

        // Disable paging controls to prevent race conditions
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
        jumpBtn.setDisable(true);
        pageJumpTF.setDisable(true);

        int pageSize = sizeComboBox.getValue();
        int totalPages = (int) Math.ceil((double) bean.total / pageSize);
        if (page < 1 || (totalPages > 0 && page > totalPages)) {
            DataUtil.showAlert(Alert.AlertType.ERROR, null, "页码必须在 1 到 " + totalPages + " 之间").showAndWait();
            stackPane.getChildren().remove(overlay);
            updatePagingControl(); // Re-enable controls
            return;
        }

        String queryText = DataUtil.replaceString(currentTab.getText());
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                String url = client.getParam(isAll.isSelected(), page) + helper.encode(queryText);
                HashMap<String, String> result = helper.getHTML(url, 120000, 120000);
                Platform.runLater(() -> {
                    try {
                        if (result.get("code").equals("200")) {
                            JSONObject obj = JSON.parseObject(result.get("msg"));
                            if (obj.getBoolean("error")) {
                                DataUtil.showAlert(Alert.AlertType.ERROR, null, obj.getString("errmsg")).showAndWait();
                                return;
                            }
                            bean.page = page;
                            List<TableBean> list = (List<TableBean>) DataUtil.loadJsonData(bean, obj, null, null, false);
                            BorderPane borderPane = (BorderPane) stackPane.getChildren().get(0);
                            TableView<TableBean> tableView = (TableView<TableBean>) borderPane.getCenter();
                            tableView.getItems().setAll(list);
                            // 更新状态栏
                            StatusBar bar = (StatusBar) borderPane.getBottom();
                            Label totalLabel = (Label) bar.getRightItems().get(0);
                            totalLabel.setText(ResourceBundleUtil.getResource().getString("QUERY_TIPS1") +
                                    obj.getString("size") + ResourceBundleUtil.getResource().getString("QUERY_TIPS2"));
                            Label countLabel = (Label) bar.getRightItems().get(1);
                            countLabel.setText(String.valueOf(obj.getJSONArray("results").size()));
                        } else {
                            DataUtil.showAlert(Alert.AlertType.ERROR, null, "请求失败: " + result.get("msg")).showAndWait();
                        }
                    } finally {
                        stackPane.getChildren().remove(overlay);
                        updatePagingControl(); // Re-enable controls
                    }
                });
                return null;
            }
        };
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                DataUtil.showAlert(Alert.AlertType.ERROR, null, "加载页面时发生未知错误。").showAndWait();
                stackPane.getChildren().remove(overlay);
                updatePagingControl(); // Re-enable controls
            });
        });
        new Thread(task).start();
    }

    private void updatePagingControl() {
        Tab currentTab = tabPane.getCurrentTab();
        if (currentTab == null || currentTab.getText().equals(resourceBundle.getString("HOMEPAGE"))) {
            pageLabel.setText("");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            pageJumpTF.setDisable(true);
            jumpBtn.setDisable(true);
            pageJumpTF.clear();
            return;
        }
        TabDataBean bean = tabPane.getTabDataBean(currentTab);
        if (bean == null || bean.total == 0) {
            pageLabel.setText("0/0");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            pageJumpTF.setDisable(true);
            jumpBtn.setDisable(true);
            pageJumpTF.clear();
            return;
        }
        int pageSize = sizeComboBox.getValue();
        int totalPages = (int) Math.ceil((double) bean.total / pageSize);
        pageLabel.setText(bean.page + "/" + totalPages);
        prevBtn.setDisable(bean.page <= 1);
        nextBtn.setDisable(bean.page >= totalPages);
        pageJumpTF.setDisable(false);
        jumpBtn.setDisable(false);
        pageJumpTF.setPromptText(bean.page + "/" + totalPages);
        pageJumpTF.clear();
    }
}