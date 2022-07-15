module com.mycompany.sb_business {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires java.desktop;

    opens com.mycompany.sb_business to javafx.fxml;
    exports com.mycompany.sb_business;
}
