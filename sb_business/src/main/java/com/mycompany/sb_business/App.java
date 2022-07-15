package com.mycompany.sb_business;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Alert;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Scene scene;
    private static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static String URL_DRIVER = "jdbc:mysql://127.0.0.1:3308/sb_business";
    private static String USERNAME = "root";
    private static String PASSWORD = "Ajlc_db";
    protected static Connection con;
    protected static String search = "";
    final static String INVALID = "-fx-border-color: red;";
    final static String WHITE = "-fx-background-color: white;";
    final static String DEFAULT = "-fx-background-color: #d3d3d3;";
    private static LoginSession session;

    @Override
    public void start(Stage stage) throws IOException {
//        scene = new Scene(loadFXML("login"), 280, 230);
//        scene = new Scene(loadFXML("primary"), 1140, 565);
        scene = new Scene(loadFXML("login"), 1140, 565);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    static void showErrorAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(context);
        alert.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    
    protected static int prepareQuery(String query, String[] param, String datatype) throws SQLException {
        PreparedStatement st = preparedStatement(query, param, datatype);
        int affectedRows = st.executeUpdate();
        st.close();
        return affectedRows;
    }
  
    protected static ResultSet preparedSelect(String query, String[] param, String datatype) throws SQLException {
        PreparedStatement st = preparedStatement(query, param, datatype);
        ResultSet rs = st.executeQuery();
//        st.close();
//        rs.close();
        return rs;
    }
    
    protected static ResultSet querySelect(String query) throws SQLException {
        return con.createStatement().executeQuery(query);
    }  
 
    protected static int query(String query) throws SQLException {
        return con.createStatement().executeUpdate(query);
    }  
    
    private static PreparedStatement preparedStatement(String query, String[] param, String datatype) throws SQLException, NumberFormatException {
        final int length = param.length;
        PreparedStatement st = con.prepareStatement(query);
        if (datatype == null) {
            datatype = "s".repeat(length);
        }
        for(int i = 0; i < length; i++) {
            int ind = i + 1;
            switch (datatype.charAt(i)) {
                case 's':
                    st.setString(ind, param[i]);
                    break;
                case 'i':
                    st.setInt(ind, Integer.parseInt(param[i]));
                    break;
                case 'd':
                    st.setDouble(ind, Double.parseDouble(param[i]));
                    break;
                case 't':
                    st.setDate(ind, Date.valueOf(param[i]));
                    break;
            }
        }
        return st;
    }

    protected static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        try {
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL_DRIVER, USERNAME, PASSWORD);
            System.out.println("Connection successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch();
    }

}