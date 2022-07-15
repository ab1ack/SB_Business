package com.mycompany.sb_business;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML private Button loginBtn;
    @FXML private TextField userIDFld;
    @FXML private PasswordField passwordFld;
    @FXML private Label error_msg;

    @FXML
    private void login(ActionEvent event) throws IOException, SQLException {
        String uid = userIDFld.getText().trim();
        String password = passwordFld.getText();

        ResultSet rs = App.preparedSelect(
                "select user_id, first_name, last_name from user where user_id = ? and password = ?; ",
                    new String[] {uid, password}, null
                );
        if (rs.next()) {
            LoginSession.userID = rs.getString("user_id");
            LoginSession.userFirst = rs.getString("first_name");
            LoginSession.userLast = rs.getString("last_name");
            LoginSession.isLoggedIn = true;
            error_msg.setText("");

            // Close login window
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
            // Open primary window
            Stage stage = new Stage();
            stage.setTitle("Record Form");
            stage.setScene(new Scene(App.loadFXML("primary"), 1140, 565));
            stage.setResizable(false);
            stage.show();
        } else {
            error_msg.setText("Error login");
        }
    }
}