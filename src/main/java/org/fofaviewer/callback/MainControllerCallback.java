package org.fofaviewer.callback;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.List;

/**
 * MainController回调，用于在线程中设置调用MainController的方法
 */
public interface MainControllerCallback {

    default void queryCall(List<String> strList){}

    default void setStatusBar(){}

    default CheckBox getOsCheckBox() { return new CheckBox(); }
    default CheckBox getIcpCheckBox() { return new CheckBox(); }
    default CheckBox getProductCheckBox() { return new CheckBox(); }
    default CheckBox getLastUpdateTimeCheckBox() { return new CheckBox(); }
    default CheckBox getWithFidCheckBox() { return new CheckBox(); }
    default CheckBox getCertsOrgCheckBox() { return new CheckBox(); }
    default CheckBox getCertsCnCheckBox() { return new CheckBox(); }
}
