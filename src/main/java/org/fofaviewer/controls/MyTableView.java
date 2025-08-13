package org.fofaviewer.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.util.converter.IntegerStringConverter;
import org.fofaviewer.bean.TableBean;
import org.fofaviewer.callback.MainControllerCallback;
import org.fofaviewer.utils.DataUtil;
import org.fofaviewer.utils.RequestUtil;
import org.fofaviewer.utils.ResourceBundleUtil;
import org.tinylog.Logger;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * TableView装饰类
 */
public class MyTableView {
    private final ResourceBundle resourceBundle = ResourceBundleUtil.getResource();
    private final RequestUtil helper = RequestUtil.getInstance();
    
    public MyTableView(TableView<TableBean> view, MainControllerCallback mainControllerCallback) {
        view.setEditable(true); // 允许表格编辑
        TableColumn<TableBean, Integer> num = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_NO"));
        TableColumn<TableBean, String> host = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_HOST"));
        TableColumn<TableBean, String> title = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_TITLE"));
        TableColumn<TableBean, String> ip = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_IP"));
        TableColumn<TableBean, Integer> port = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_PORT"));
        TableColumn<TableBean, String> domain = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_DOMAIN"));
        TableColumn<TableBean, String> protocol = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_PROCOTOL"));
        TableColumn<TableBean, String> server = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_SERVER"));
        TableColumn<TableBean, String> os = new TableColumn<>("os");
        TableColumn<TableBean, String> icp = new TableColumn<>("icp");
        TableColumn<TableBean, String> product = new TableColumn<>(resourceBundle.getString("PRODUCT_FINGER"));
        TableColumn<TableBean, String> lastupdatetime = new TableColumn<>(resourceBundle.getString("LAST_UPDATE_TIME"));
        TableColumn<TableBean, String> fid = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_FID"));
        TableColumn<TableBean, String> certOrg = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_CERTORG"));
        TableColumn<TableBean, String> certCN = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_CERTCN"));
        TableColumn<TableBean, String> country = new TableColumn<>("国家");
        TableColumn<TableBean, String> region = new TableColumn<>("区域");
        TableColumn<TableBean, String> city = new TableColumn<>("城市");

        num.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().num));
        host.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().host));
        title.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().title));
        ip.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().ip));
        port.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().port));
        domain.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().domain));
        protocol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().protocol));
        server.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().server));
        os.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().os));
        icp.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().icp));
        product.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().product));
        lastupdatetime.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().lastupdatetime));
        fid.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().fid));
        certOrg.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().certOrg));
        certCN.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().certCN));
        country.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().country));
        region.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().region));
        city.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().city));

        // 设置单元格为TextFieldTableCell，使其可编辑/复制
        host.setCellFactory(TextFieldTableCell.forTableColumn());
        title.setCellFactory(TextFieldTableCell.forTableColumn());
        ip.setCellFactory(TextFieldTableCell.forTableColumn());
        domain.setCellFactory(TextFieldTableCell.forTableColumn());
        protocol.setCellFactory(TextFieldTableCell.forTableColumn());
        server.setCellFactory(TextFieldTableCell.forTableColumn());
        os.setCellFactory(TextFieldTableCell.forTableColumn());
        icp.setCellFactory(TextFieldTableCell.forTableColumn());
        product.setCellFactory(TextFieldTableCell.forTableColumn());
        lastupdatetime.setCellFactory(TextFieldTableCell.forTableColumn());
        fid.setCellFactory(TextFieldTableCell.forTableColumn());
        certOrg.setCellFactory(TextFieldTableCell.forTableColumn());
        certCN.setCellFactory(TextFieldTableCell.forTableColumn());
        country.setCellFactory(TextFieldTableCell.forTableColumn());
        region.setCellFactory(TextFieldTableCell.forTableColumn());
        city.setCellFactory(TextFieldTableCell.forTableColumn());
        port.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // 修改ip的排序规则
        ip.setComparator(Comparator.comparing(DataUtil::getValueFromIP));
        view.getColumns().add(num);
        view.getColumns().addAll(new ArrayList<TableColumn<TableBean,String>>(){{ add(host);add(title);add(ip);}});
        view.getColumns().add(port);
        view.getColumns().addAll(new ArrayList<TableColumn<TableBean,String>>(){{
            add(domain);add(protocol);add(os);add(icp);add(certCN);add(certOrg);add(server);add(product);add(fid);add(lastupdatetime);add(country);add(region);add(city);
        }});
        
        // 启用单元格选择
        view.getSelectionModel().setCellSelectionEnabled(true);
        view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 添加键盘事件以处理复制操作
        final KeyCodeCombination keyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        view.setOnKeyPressed(event -> {
            if (keyCodeCombination.match(event)) {
                copySelectionToClipboard(view);
            }
        });

        view.setRowFactory(param -> {
            final TableRow<TableBean> row = new TableRow<>();
            // 设置表格右键菜单
            ContextMenu rowMenu = new ContextMenu();
            MenuItem copyLink = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_COPYLINK"));
            copyLink.setOnAction(event -> {
                ObservableList<TableBean> items =  view.getSelectionModel().getSelectedItems();
                ClipboardContent content = new ClipboardContent();
                if(items.size() > 1){
                    StringBuilder builder = new StringBuilder();
                    for(TableBean bean : items){
                        builder.append(bean.host).append("\n");
                    }
                    content.putString(builder.toString());
                }else{
                    content.putString(items.get(0).host);
                }
                Clipboard.getSystemClipboard().setContent(content);
            });
            MenuItem queryIp = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_IP"));
            queryIp.setOnAction(event -> {
                String _ip = row.getItem().ip;
                mainControllerCallback.queryCall(new ArrayList<String>(){{add("ip=\"" + _ip + "/32" + "\"");}});
            });
            MenuItem queryCSet = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_C-CLASS"));
            queryCSet.setOnAction(event -> {
                String _ip = row.getItem().ip;
                mainControllerCallback.queryCall(new ArrayList<String>(){{add("ip=\""+ _ip.substring(0, _ip.lastIndexOf('.')) + ".0/24" + "\"");}});
            });
            MenuItem querySubdomain = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_DOAMIN"));
            querySubdomain.setOnAction(event -> {
                String domain1 = row.getItem().domain;
                if(domain1 != null && !domain1.isEmpty()){
                    mainControllerCallback.queryCall(new ArrayList<String>(){{add("domain=\""+ domain1 + "\"");}});
                }
            });
            MenuItem queryFavicon = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_FAVICON"));
            queryFavicon.setOnAction(event -> {
                String url = row.getItem().host;
                if(!url.startsWith("http")){
                    url = "http://" + url;
                    if(url.endsWith("443")){
                        url = "https://" + url.substring(0, url.length()-4);
                    }
                }
                String link = helper.getLinkIcon(url); // 请求获取link中的favicon链接
                HashMap<String,String> res;
                if(link !=null){
                    res = helper.getImageFavicon(link);
                }else{
                    res = helper.getImageFavicon(url + "/favicon.ico");
                }
                if(res != null){
                    if(res.get("code").equals("error")){
                        DataUtil.showAlert(Alert.AlertType.ERROR, null, res.get("msg")).showAndWait();return;
                    }
                    mainControllerCallback.queryCall(new ArrayList<String>(){{add(res.get("msg"));}});
                    return;
                }
                DataUtil.showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("QUERY_FAVICON_ERROR")).showAndWait();
            });
            MenuItem queryCert = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_CERT"));
            queryCert.setOnAction(event -> {
                String sn = row.getItem().certCN;
                String _protocol = row.getItem().protocol;
                if(_protocol.equals("https")){
                    if(sn == null || sn.isEmpty()){
                        DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("QUERY_CERT_ERROR")).showAndWait();
                    }else{
                        mainControllerCallback.queryCall(new ArrayList<String>(){{add("cert=\"" + sn + "\"");}});
                    }
                }
            });
            MenuItem queryCertOrg = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_CERTORG"));
            queryCertOrg.setOnAction(event -> {
                String org = row.getItem().certOrg;
                if(org != null && !org.isEmpty()){
                    mainControllerCallback.queryCall(new ArrayList<String>(){{add("cert.subject.org=\"" + org + "\"");}});
                }
            });
            MenuItem queryTitle = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_TITLE"));
            queryTitle.setOnAction(event -> {
                String _title = row.getItem().title;
                if(_title != null && !_title.isEmpty()){
                    mainControllerCallback.queryCall(new ArrayList<String>(){{add("title=\""+ _title + "\"");}});
                }
            });

            MenuItem fidMenu = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_FID"));
            fidMenu.setOnAction(event -> {
                String _fid = row.getItem().fid;
                if(_fid != null && !_fid.isEmpty()){
                    mainControllerCallback.queryCall(new ArrayList<String>(){{add("fid=\""+_fid+"\"");}});
                }else{
                    DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("QUERY_FID_ERROR")).showAndWait();
                }
            });
            rowMenu.getItems().addAll(copyLink, queryIp,queryTitle, queryCSet, querySubdomain, queryFavicon, queryCert , fidMenu,queryCertOrg);
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            return row;
        });
        view.getSortOrder().add(num);
    }

    private void copySelectionToClipboard(TableView<TableBean> tableView) {
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
}

