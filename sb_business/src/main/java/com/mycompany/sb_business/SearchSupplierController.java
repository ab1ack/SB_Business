package com.mycompany.sb_business;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SearchSupplierController implements Initializable {
    protected ObservableList<Supplier> result;
    final String INVALID = "-fx-border-color: red;";
    final String WHITE = "-fx-background-color: white;";
    @FXML private TextField supplier_id;
    @FXML private TextField search_fld;
    @FXML private Button cancel_btn;
    @FXML private Button search_btn;
    @FXML private Button choose_btn;
    @FXML private Button edit_btn;
    @FXML private Button delete_btn;
    @FXML private Label errorMsg;
    @FXML private ListView<Supplier> resultList;
    
    private Alert alert;
    
    @FXML
    private void cancel() throws IOException {
        ((Stage) cancel_btn.getScene().getWindow()).close();
    }
    
    @FXML
    private void searchSupplier() throws IOException {
        String keyword = search_fld.getText().trim();
        refreshList("select * from supplier where supplier_id like '%" + keyword 
                    + "%' or supplier_id like '%" + keyword + "%';");
    }
    
     @FXML
    private void addSupplier() throws IOException {
        showSupplierForm("add");
    }
    
    private void showSupplierForm(String action) throws IOException {
        AddSupplierController.action = action;
        Stage stage = new Stage();
        stage.setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " supplier");
        stage.setResizable(false);
        stage.setScene(new Scene(App.loadFXML("addSupplier"), 280, 165));
        stage.showAndWait();
    }
    
    @FXML
    private void editSupplier() throws IOException {
        AddSupplierController.currentSupId = getSelectedItemId();
        showSupplierForm("edit");
        showAll();
    }
    
    @FXML
    private void deleteSupplier() throws IOException {
        try {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Delete Supplier");
            alert.setHeaderText("Delete selected supplier?");
            alert.setContentText(null);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                int affectedRows = App.prepareQuery( "delete from supplier where supplier_id = ?;", new String[]{ getSelectedItemId()}, null);
                System.out.println("Affected row/s: " + affectedRows);
                showAll();
            }else{
                alert.close();
            }
        } catch (SQLException ignore) {}
    }
    
    
    @FXML
    private void chooseSupplier() throws IOException {
        PrimaryController.search = getSelectedItemId();
        cancel();
    }

    private String getSelectedItemId() {
        return resultList.getSelectionModel().getSelectedItem().getId();
    }

    
    private void refreshList(String qry) {
         try {
            ResultSet rs = App.querySelect((qry == null) ? "select * from supplier;" : qry);
            result.clear();
            while(rs.next()) {
                result.add(new Supplier(rs.getString("supplier_id"), rs.getString("supplier_name")));
            }
        } catch (SQLException ignore) {}
    }
    
    @FXML 
    private void showAll() throws IOException {
        refreshList(null);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        result = FXCollections.observableArrayList();
        refreshList(null);
        resultList.setItems(result);
        choose_btn.disableProperty().bind(resultList.getSelectionModel().selectedItemProperty().isNull());
        edit_btn.disableProperty().bind(resultList.getSelectionModel().selectedItemProperty().isNull());
        delete_btn.disableProperty().bind(resultList.getSelectionModel().selectedItemProperty().isNull());
    }
    
    
}