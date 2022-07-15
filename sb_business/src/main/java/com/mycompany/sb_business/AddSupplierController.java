package com.mycompany.sb_business;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddSupplierController implements Initializable {
    protected static String action = "add";
    protected static String currentSupId;
    final String INVALID = "-fx-border-color: red;";
    final String WHITE = "-fx-background-color: white;";
    @FXML private TextField supplier_id;
    @FXML private TextField supplier_name;
    @FXML private Button action_btn;
    @FXML private Button cancel_btn;
    @FXML private Label errorMsg;
    
    @FXML
    private void cancel() throws IOException {
        ((Stage) cancel_btn.getScene().getWindow()).close();
    }
    
    private void saveChanges(String qry, String change) throws IOException {
        System.out.println(qry);
        final String supID = supplier_id.getText().trim();
        final String name = supplier_name.getText().trim();
        
        if (supID.isBlank() && name.isBlank()) { // Both fields are empty
            errorMsg.setText("Please complete the fields");
            supplier_id.setStyle(INVALID);
            supplier_name.setStyle(INVALID);
        } else {
            if (supID.isBlank()) {      // Supplier ID is empty
                errorMsg.setText("Supplier ID is required");
                supplier_id.setStyle(INVALID);
            } else {
                supplier_id.setStyle(WHITE);
                if (name.isBlank()) {   // Supplier name is empty
                    errorMsg.setText("Supplier name is required");
                    supplier_name.setStyle(INVALID);
                } else {                // Fields are not empty
                    supplier_name.setStyle(WHITE);
                    if (!supID.matches("\\S+")) {   // Supplier ID has space
                        errorMsg.setText("Supplier ID should not have whitespaces");
                    } else {
                        String[] params = new String[] {supID, name};
                        if (change.equals("edit")) {
                            params = new String[] {supID, name, supID};
                        }
                        try {
                            App.prepareQuery(qry, params, null);
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setHeaderText(String.format("Supplier information %sed", action));
                            alert.show();
                            
                            if (change.equals("add")) {
                                supplier_id.clear();
                                supplier_name.clear();
                                errorMsg.setText("");
                            } else { 
                                cancel(); // close supplier form after edit
                            }
                            
                        } catch (SQLException e) {          
                            errorMsg.setText("Specified ID is not unique");
                            supplier_id.setStyle(INVALID);
                        }
                        supplier_id.requestFocus();
                    }
                }
            }
        }
    }
    
    @FXML
    private void addSupplier() throws IOException {
        saveChanges("insert into supplier values(?, ?);", "add");
    }
    
    @FXML
    private void editSupplier() throws IOException {
        saveChanges("update supplier set supplier_id=?, supplier_name=? where supplier_id=?;", "edit");
    }

    private void populateFields(String supId) {
        try {
            ResultSet rs = App.querySelect("select * from supplier where supplier_id='" + supId + "';");
            rs.next();
            supplier_id.setText(rs.getString("supplier_id"));
            supplier_name.setText(rs.getString("supplier_name"));
        } catch (SQLException sQLException) {}
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (action.equals("add")) {
            action_btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        addSupplier();
                    } catch (IOException ex) {}
                }
            });
        } else if (action.equals("edit")) {
            action_btn.setText("Edit");
            action_btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                         editSupplier();
                    } catch (IOException ex) {}
                }
            });
            populateFields(AddSupplierController.currentSupId);
        }
    }
}
