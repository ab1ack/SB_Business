package com.mycompany.sb_business;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {
  @FXML private TableView<Report> reportTableView;
  @FXML private TableColumn<Report, String> supplierCol;
  @FXML private TableColumn<Report, String> datePeriodCol;
  @FXML private TableColumn<Report, Void> actionCol;
  @FXML private TextField searchFld;

  private ObservableList<Report> data;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    refreshReportList(null);
    addButtonToTable();
    searchFld.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
          try {
            search();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });
    // allow table to select more than 1 item
    reportTableView.getSelectionModel().setSelectionMode(
        SelectionMode.MULTIPLE
    );
  }

  @FXML
  private void showAll() throws IOException {
    refreshReportList("");
  }

  private void refreshReportList(String keyword) {
    File f = new File("D:/SB_Business/reports");
    String[] pathnames = f.list();
    data = FXCollections.observableArrayList();

    // For each pathname in the pathnames array
    for (String pathname : pathnames) {
      // Print the names of files and directories
      Report report = new Report(pathname);
      if (keyword != null) {
        if (pathname.toLowerCase().contains(keyword.toLowerCase()))  data.add(report);
      } else data.add(report);
    }
    supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
    datePeriodCol.setCellValueFactory(new PropertyValueFactory<>("datePeriod"));
    reportTableView.setItems(data);
  }

  private void addButtonToTable() {
    Callback<TableColumn<Report, Void>, TableCell<Report, Void>> cellFactory = new Callback<>() {
      @Override
      public TableCell<Report, Void> call(final TableColumn<Report, Void> param) {
        final TableCell<Report, Void> cell = new TableCell<>() {

          private final Button openBtn = new Button("Open");
          private final Button deleteBtn = new Button("Delete");
          {
            openBtn.setOnAction((ActionEvent event) -> {
              Report data = getTableView().getItems().get(getIndex());
              data.open();
            });
          }
          {
            deleteBtn.setOnAction((ActionEvent event) -> {
              Report data = getTableView().getItems().get(getIndex());
              data.delete(true);
              refreshReportList(null);
            });
          }

          @Override
          public void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setGraphic(null);
            } else {
              HBox pane = new HBox(openBtn, deleteBtn);
              pane.setSpacing(5);
              pane.setAlignment(Pos.TOP_CENTER);
              setGraphic(pane);
            }
          }
        };
        return cell;
      }
    };
    actionCol.setCellFactory(cellFactory);
  }


  @FXML
  private void search() throws IOException {
    data.clear();
    refreshReportList(searchFld.getText().trim());
  }

  @FXML
  private void openReport() throws IOException {
    getSelectedItems().forEach(Report::open);
  }

  @FXML
  private void deleteReport() throws IOException {
    ObservableList<Report> list = getSelectedItems();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("Delete Confirmation");
    alert.setContentText("Are you sure you're going to delete " + list.size() + " selected report" + (list.size() > 1 ? "s? " : "?"));
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      list.forEach(e -> e.delete(false));
      refreshReportList(null);
    }
  }

  private ObservableList<Report> getSelectedItems() {
    return reportTableView.getSelectionModel().getSelectedItems();
  }
}
