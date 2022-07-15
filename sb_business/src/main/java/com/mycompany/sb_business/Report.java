package com.mycompany.sb_business;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.io.File;
import java.util.Optional;

public class Report {
  private File file;
  private String pathName = "D:/SB_Business/reports/";
  private String supplier;
  private String datePeriod;
//  private  action;
  public Report(String pathName) {
    this.pathName += pathName;
    file = new File(this.pathName);
    String[] pathDetails = pathName.split("_");
    this.supplier = pathDetails[0];
    datePeriod = pathDetails[1].replaceAll("\\.docx", "");
  }

  public String getSupplier() {
    return supplier;
  }

  public void setSupplier(String supplier) {
    this.supplier = supplier;
  }

  public String getDatePeriod() {
    return datePeriod;
  }

  public void setDatePeriod(String datePeriod) {
    this.datePeriod = datePeriod;
  }

  public void open() {
    try {
      //check if Desktop is supported by Platform or not
      if(!Desktop.isDesktopSupported()) {
        System.out.println("not supported");
        return;
      }
      Desktop desktop = Desktop.getDesktop();
      if(file.exists())         //checks file exists or not
        desktop.open(file);              //opens the specified file
    } catch (Exception ignored) {}
  }

  public void delete(Boolean withAlert) {
    if (withAlert) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setHeaderText("Delete Confirmation");
      alert.setContentText("Are you sure you're going to delete this report? ");
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        file.delete();
      }
    } else {
      file.delete();
    }
  }
}
