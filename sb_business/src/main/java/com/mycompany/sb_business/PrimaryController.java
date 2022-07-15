package com.mycompany.sb_business;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PrimaryController implements Initializable {
  private static final String FORMATTER = "%,.2f";
  private static final String DATE_QRY_FORMAT = "YYYY-MM-dd";
  private static final String DATE_FORMAT = "MM-dd-yyyy";
  private static final long MARGIN = 720L;
  public static final String D_SB_BUSINESS_REPORTS_S_S_DOCX = "D:\\SB_Business\\reports\\%s_%s.docx";
  private static ArrayList<String> SIZEVALUES = new ArrayList<>(Arrays.asList("Small", "Assorted", "Medium", "Jumbo", "Reject"));

  private ObservableList<Entry> data;
  private static Integer recordId = null;
  private static Entry currentEntry = null;

  @FXML
  private Label userField;
  @FXML
  private Label subTotal_label;
  @FXML
  private Label total_label;
  @FXML
  private Label less_label;
  @FXML
  private Button delete_btn;
  @FXML
  private Button edit_btn;
  @FXML
  private Button enter_btn;
  @FXML
  private Button saveEntry_btn;
  @FXML
  private Button search_btn;
  @FXML
  private Button record_adv_btn;
  @FXML
  private TextField sup_name;
  @FXML
  private TextArea kilo_area;
  @FXML
  private TextArea less_area;
  @FXML
  private TextField total_weight;
  @FXML
  private TextField total_amount;
  @FXML
  private TextField advance_fld;
  @FXML
  private TextField fare_fld;
  @FXML
  private CheckBox sz_small;
  @FXML
  private CheckBox sz_medium;
  @FXML
  private CheckBox sz_assorted;
  @FXML
  private CheckBox sz_jumbo;
  @FXML
  private CheckBox sz_reject;
  @FXML
  private CheckBox sz_others;
  @FXML
  private TextField sz_otherFld;
  @FXML
  private TextField price_field;
  @FXML
  private DatePicker date;
  @FXML
  private DatePicker from_date;
  @FXML
  private DatePicker to_date;

  @FXML
  private TableView<Entry> table;
  @FXML
  private TableColumn<Entry, String> dateCol;
  @FXML
  private TableColumn<Entry, String> sizeCol;
  @FXML
  private TableColumn<Entry, String> grossCol;
  @FXML
  private TableColumn<Entry, String> lessCol;
  @FXML
  private TableColumn<Entry, Double> netCol;
  @FXML
  private TableColumn<Entry, Double> priceCol;
  @FXML
  private TableColumn<Entry, Double> totalCol;

  protected static String search = "";

  private List<String> getWeight(String id) throws IOException {
    String[] stringArray;
    if (id.equals("kilo_area")) {
      stringArray = kilo_area.getText().split("\n");
    } else {
      stringArray = less_area.getText().split("\n");
      if (stringArray[0].trim().length() == 0) {
          stringArray[0] = "0";
      }
    }
    return Arrays.stream(stringArray)
      .map(String::trim)
      .collect(Collectors.toList());
  }

  @FXML
  private void refresh() throws IOException {
    if (!sup_name.getText().isEmpty()) chooseSupplier();
  }

  @FXML
  private void editEntry(ActionEvent event) {
    Entry selectedEnt = table.getSelectionModel().getSelectedItem();
    currentEntry = selectedEnt;
    date.setValue(currentEntry.getLocalDate());
    kilo_area.setText(selectedEnt.getGrossTAValue());
    less_area.setText(selectedEnt.getLessTAValue());
    price_field.setText(String.format(FORMATTER, selectedEnt.getPrice()));
    // Select size
    String[] sizes = selectedEnt.getSize().split(",");
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    Scene scene = stage.getScene();
    for (String size: sizes) {
      String current = size.trim();
      System.out.println("current: " + current);

      if (SIZEVALUES.contains(current)) {
        System.out.println("included");
        current = "#sz_" + current.toLowerCase();
        ((CheckBox) scene.lookup(current)).setSelected(true);
      } else {
        ((CheckBox) scene.lookup("#sz_others")).setSelected(true);
        ((TextField) scene.lookup("#sz_otherFld")).setText(current);
      }
    }

    total_weight.setText(String.format(FORMATTER, selectedEnt.getTotalWeight()));
    total_amount.setText(String.format(FORMATTER, selectedEnt.getTotalAmount()));
    editMode(true);
    data.remove(currentEntry);
  }

  private void editMode(Boolean isEdit) {
    saveEntry_btn.setDisable(!isEdit);
    date.setDisable(isEdit);
    edit_btn.setDisable(isEdit);
    enter_btn.setDisable(isEdit);
  }

  @FXML
  private void saveEntry() throws IOException, SQLException {
    editMode(false);
    int id = currentEntry.getEntryId();
    String size = getSizeVal();
    Entry newEntry = new Entry(
        id, date.getValue(), getWeight("kilo_area"), getWeight("less_area"),
        size,
        Double.parseDouble(price_field.getText()),
        Double.parseDouble(total_weight.getText()),
        Double.parseDouble(total_amount.getText().replaceAll(",", ""))
    );
    data.remove(currentEntry);
    data.add(newEntry);
    calculateCurrentData();
    App.query(newEntry.getUpdateQry());
    currentEntry = null;
    clearFields();
  }

  @FXML
  private void deleteEntry() throws IOException {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("Delete Confirmation");
    alert.setContentText("Are you sure you want to delete this entry?");
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() != ButtonType.OK) {
      alert.close();
    } else {
      Entry entry = table.getSelectionModel().getSelectedItem();
      table.getItems().removeAll(entry);
      try {
        char size = '0';

        try {
          size = entry.getSize().toLowerCase().charAt(0);
        } catch (StringIndexOutOfBoundsException ignore) {}

        if (size == 'a' || size == 'f') {
          System.out.println(entry.getDeleteQry(true, getRecordId(sup_name.getText(), entry.getLocalDate()), size));
          App.query(entry.getDeleteQry(true, getRecordId(sup_name.getText(), entry.getLocalDate()), size));
        } else {
          App.query(entry.getDeleteQry(false, 0, null));
        }
        clearFields();
      } catch (SQLException ignored) {}
      calculateCurrentData();
    }
  }



  @FXML
  private void searchSupplier() throws IOException {
    search_btn.setDisable(true);
    Scene scene = new Scene(App.loadFXML("searchSupplier"), 270, 275);
    Stage stage = new Stage();
    stage.setTitle("Search supplier");
    stage.setResizable(false);
    stage.setScene(scene);
    stage.showAndWait();

    sup_name.setText(search);
    if (!search.isEmpty()) {
      chooseSupplier();
    }
    search_btn.setDisable(false);
  }

  private boolean isInvalidInput(String input) {
    return !input.matches("^\\d*\\.?\\d*$");
  }

  @FXML
  private void recordEntry() throws IOException {
    String supplier = sup_name.getText().trim();
    List<String> gross = getWeight("kilo_area");
    List<String> less = getWeight("less_area");
    double price = 0;
    String sizeVal = getSizeVal();
    int errorCounter = 0;

    // validate price field
    try {
      String priceText = price_field.getText().trim();
      if (priceText.isEmpty()) {
        price_field.setStyle(App.INVALID);
        errorCounter++;
      }
      price = Double.parseDouble(priceText);
      price_field.setStyle(App.WHITE);
    } catch (Exception e) {
      price_field.setStyle(App.INVALID);
      errorCounter++;
    }

    errorCounter += validateRecord(supplier, gross, less);

    if (errorCounter > 0) {
      App.showErrorAlert("Invalid Input", null);
    } else {
      LocalDate dateEntered = date.getValue();

      Entry entry = new Entry(dateEntered, gross, less, price, sizeVal);
      total_weight.setText(String.format(FORMATTER, entry.getTotalWeight()));
      total_amount.setText(String.format(FORMATTER, entry.getTotalAmount()));

      checkRecordID(supplier, dateEntered);
      try {
        Statement st = App.con.createStatement();
        st.executeUpdate(entry.getQryValue(recordId), PreparedStatement.RETURN_GENERATED_KEYS);
        ResultSet rs = st.getGeneratedKeys();
        rs.next();
        entry.setEntryId(rs.getInt(1));
      } catch (SQLException ignored) {}

      // calculate current data
      data.add(entry);
      calculateCurrentData();

      kilo_area.clear();
      less_area.clear();
      price_field.clear();
      advance_fld.clear();
      fare_fld.clear();
      clearSizeCBxs();
    }
  }

  private void calculateCurrentData() {
    var less = new Object() {
      double value = 0;
    };
    var subTotal = new Object() {
      double value = 0;
    };

    data.forEach(entry -> {
      if (isEntryAdvance(entry)) {
        less.value += entry.getTotalAmount();
      } else {
        subTotal.value += entry.getTotalAmount();
      }
    });

    double amount = subTotal.value + less.value;
    subTotal_label.setText(String.format("%,.2f", subTotal.value));
    less_label.setText(String.format("(%,.2f)", -less.value));
    total_label.setText(String.format("%,.2f", amount));
  }

  private boolean isEntryAdvance(Entry entry) {
    return entry.getSize().equals("Advance") || entry.getSize().equals("Fare");
  }

  @FXML
  private void recordAdvances(ActionEvent event) throws IOException {
    String supplier = sup_name.getText().trim();
    if (supplier.isEmpty()) {
      App.showErrorAlert("Invalid Input", "Please select supplier");
    } else {
      LocalDate dateEntered = date.getValue();

      double advance = 0;
      double fare = 0;

      // get advance and fare values
      try {
        advance = Double.parseDouble(advance_fld.getText().trim());
        fare = Double.parseDouble(fare_fld.getText().trim());
      } catch (NumberFormatException ignore) {}

      if (advance > 0 || fare > 0) {
//        checkRecordID(supplier, dateEntered);

        if (advance > 0) {
          executeAdvValQry(getRecordId(supplier, dateEntered), advance, "a");
        }

        if (fare > 0) {
          executeAdvValQry(getRecordId(supplier, dateEntered), fare, "f");
        }

        Alert alert = new Alert (Alert.AlertType.INFORMATION);
        alert.setHeaderText("Advance/s updated");
        alert.setContentText(null);
        alert.show();
      } else {
        App.showErrorAlert("Invalid Input", "Provide value for advance and/or fare");
      }
    }
  }

  private void checkRecordID(String supplier, LocalDate dateEntered) {
    PreparedStatement ps;
    try {
      ps = App.con.prepareStatement("select record_id from record where supplier_id = ? and date = ?;");
      ps.setString(1, supplier);
      ps.setDate(2,  getDateSql(dateEntered));
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        recordId = rs.getInt(1);
      } else {
        initializeRecordId(supplier, dateEntered);
      }
    } catch (SQLException ignored) {}
  }

  private void executeAdvValQry(int recordId, Double amount, String type) {
    PreparedStatement ps;
    try {
      ps = App.con.prepareStatement("select id from advance where record_id = ? and type = ?;");
      ps.setInt(1, recordId);
      ps.setString(2,  type);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        executeQry(String.format("update advance set amount = '%f' where id = '%d';", amount,  rs.getInt(1)));
      } else {
        executeQry(String.format("insert into advance (record_id, amount, type) values (%d, %f, '%s');", recordId, amount, type));
        Entry entry = new Entry(type, -amount);
        entry.setLocalDate(date.getValue());
        data.add(entry);
      }
      calculateCurrentData();
    } catch (SQLException ignored) {}
  }

  private void executeQry(String qry) {
    try {
      App.con.createStatement().executeUpdate(qry, PreparedStatement.RETURN_GENERATED_KEYS);
    } catch (SQLException ignored) {}
  }

  private String getSizeVal() {
    List<String> sizes = new ArrayList<>();
    if (sz_small.isSelected()) {
      sizes.add("Small");
    }
    if (sz_medium.isSelected()) {
      sizes.add("Medium");
    }
    if (sz_assorted.isSelected()) {
      sizes.add("Assorted");
    }
    if (sz_jumbo.isSelected()) {
      sizes.add("Jumbo");
    }
    if (sz_reject.isSelected()) {
      sizes.add("Reject");
    }
    if (sz_others.isSelected()) {
      sizes.add(sz_otherFld.getText().trim());
    }
    return String.join(", ", sizes);
  }

  private java.sql.Date getDateSql(LocalDate dateEntered) {
    return java.sql.Date.valueOf(getDateFormat(dateEntered));
  }

  private int validateRecord(String supplier, List<String> gross, List<String> less) {
    int errorCounter = 0;
    var grossAreaWrapper = new Object() {
      int errorCounter = 0;
    };

    var lessAreaWrapper = new Object() {
      int errorCounter = 0;
    };

    gross.forEach(e -> {
      if (isInvalidInput(e))
        grossAreaWrapper.errorCounter++;
    });

    if (less.size() != 0) {
        less.forEach(e -> {
          if (isInvalidInput(e))
            lessAreaWrapper.errorCounter++;
        });
    }


    if (grossAreaWrapper.errorCounter > 0 || kilo_area.getText().trim().isEmpty()) {
      kilo_area.setStyle(App.INVALID);
      errorCounter++;
    } else
      kilo_area.setStyle(App.WHITE);

    if (lessAreaWrapper.errorCounter > 0 ) {
      less_area.setStyle(App.INVALID);
      errorCounter++;
    } else
      less_area.setStyle(App.WHITE);

    if (supplier.isEmpty()) {
      sup_name.setStyle(App.INVALID);
      errorCounter++;
    } else
      sup_name.setStyle(App.WHITE);

    return errorCounter;
  }

  private void initializeRecordId(String supplier, LocalDate dateEntered) {
    java.sql.Date dateSql = getDateSql(dateEntered);
    try {
      PreparedStatement ps = App.con.prepareStatement("insert into record (supplier_id, date) values (?, ?);",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, supplier);
      ps.setDate(2, dateSql);

      ps.executeUpdate();
      ResultSet rs = ps.getGeneratedKeys();
      if (rs.next()) recordId = rs.getInt(1);
      else recordId = null;
    } catch (SQLException sQLException) {
      PreparedStatement ps;
        try {
            ps = App.con.prepareStatement("select record_id from record where supplier_id = ? and date = ?;");
            ps.setString(1, supplier);
            ps.setDate(2, dateSql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                recordId = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  }

  @FXML
  private void saveRecord() throws IOException, ParseException {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("Confirmation to save records");
    alert.setContentText(null);
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() != ButtonType.OK) {
      alert.close();
    } else {
      int entryRecordId = 0;
      String supplier = sup_name.getText();
      String dateString = getDateFormat(date.getValue());
      Record record = new Record(supplier, date.getValue(), new ArrayList<>(data));

      try {
        PreparedStatement ps = App.con.prepareStatement("insert into record (supplier_id, date) values (?, ?);",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, supplier);
        ps.setDate(2, java.sql.Date.valueOf(dateString));
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        while (rs.next()) {
          entryRecordId = rs.getInt(1);
        }
      } catch (SQLException ex) {
        try {
          ResultSet rs = App.preparedSelect(
              "select record_id from record where supplier_id = ? and date = ?;",
              new String[] { supplier, dateString }, "st");
          rs.next();
          entryRecordId = rs.getInt(1);

          ArrayList<Entry> entriesToUpdate = record.getEntriesToUpdate();
          entriesToUpdate.forEach(e -> {
            try {
              App.prepareQuery(
                  "update record_entry set size=?, gross=?, less=?, net=?, price=?, total=? where entry_id=?;",
                  new String[] { e.getSize(), e.getGross(), e.getLess(), String.valueOf(e.getTotalWeight()),
                      String.valueOf(e.getPrice()), String.valueOf(e.getTotalAmount()), String.valueOf(e.getEntryId())
                  }, "sssdddi");
            } catch (SQLException ignored) {}
          });

        } catch (SQLException ignored) {}
      }

      try {
        int affectedRows = App.query(record.getInsertEntriesQry(entryRecordId));
        System.out.println("Affected rows: " + affectedRows);
      } catch (SQLException ignored) {}
    }
  }

  private String getDateFormat(LocalDate date) {
    System.out.println(date);
    return (date == null) ? null : date.format(DateTimeFormatter.ofPattern(DATE_QRY_FORMAT));
  }

  @FXML
  private void chooseSupplier() throws IOException {
    String supplier = sup_name.getText();
    LocalDate dateLD = date.getValue();
    String dateString = getDateFormat(dateLD);
    String[] param = { supplier, dateString };

//    checkRecordID(supplier, dateLD);

    // Get entries
    retrieveEntries(dateString, null, param);

    // Get advanced entries
    retrieveAdvance();

    // Put edit and delete buttons to default
    setToNoSelectedItem();

    // set supplier field to default
    sup_name.setStyle("-fx-background-color: #d3d3d3; -fx-border-color: none;");

    calculateCurrentData();
  }

  @FXML
  private void executeQry() throws  IOException {
    Alert alert;
    String supplier = sup_name.getText().trim();
    String amount = advance_fld.getText().trim();
    if (supplier.isEmpty()) {
        sup_name.setStyle(App.INVALID);
        App.showErrorAlert("Invalid Input", null);
        return;
    } else sup_name.setStyle(App.DEFAULT);

    // validation
    if (!isInvalidInput(amount)) {
      if (recordId == null) initializeRecordId(supplier, date.getValue());
      try {
        App.prepareQuery(getAdvanceQry(),
            new String[]{ amount, String.valueOf(recordId) },
            "ds"
        );
        alert = new Alert (Alert.AlertType.INFORMATION);
        alert.setHeaderText("Advance amount has been recorded/edited");
        alert.setContentText(null);
        alert.show();
      } catch (SQLException ignore) {}
    } else {
      alert = new Alert (Alert.AlertType.WARNING);
      alert.setHeaderText("Invalid input");
      alert.setContentText("Please provide a valid amount");
      alert.show();
      advance_fld.requestFocus();
    }
  }

  private String getAdvanceQry () throws SQLException{
    ResultSet rs = App.querySelect(String.format("select amount from advance where record_id=%d", recordId));
    if (rs.next()) return "update advance set amount = ? where record_id = ?;";
    return "insert into advance (amount, record_id) values (?, ?);";
  }

  private void retrieveAdvance() {
    advance_fld.setText(String.format("%,.2f", getAdvValue("a")));
    fare_fld.setText(String.format("%,.2f", getAdvValue("f")));
  }

  private double getAdvValue(String type) {
    try {
      ResultSet result = App.querySelect(String.format(
          "select amount from advance where record_id = %d and type = '%s';",
          recordId, type)
      );
      if (result.next()) return result.getDouble("amount");
    } catch (SQLException ignored) {}
    return 0;
  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    data = FXCollections.observableArrayList();

    /* Initialize table */
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
    grossCol.setCellValueFactory(new PropertyValueFactory<>("gross"));
    lessCol.setCellValueFactory(new PropertyValueFactory<>("less"));
    netCol.setCellValueFactory(new PropertyValueFactory<>("totalWeight"));
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    table.setItems(data);
    table.setOnKeyPressed(t -> {
      if (t.getCode() == KeyCode.DELETE) {
        try {
          deleteEntry();
        } catch (IOException ignored) {}
      }
    });
    table.setOnMouseClicked(t -> {
      boolean isItemSelected = table.getSelectionModel().getSelectedItems().isEmpty();
      if (!isItemSelected) {
        Entry selected = table.getSelectionModel().getSelectedItem();
        edit_btn.setDisable(selected.getSize().equals("Advance") || selected.getSize().equals("Fare"));
      }
      delete_btn.setDisable(isItemSelected);
    });

    /* Initialize date */
    from_date.setValue(LocalDate.now());
    date.setValue(LocalDate.now());

    /* Change listener of date */
    date.valueProperty().addListener(new ChangeListener<LocalDate>() {
      @Override
      public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
        if (!sup_name.getText().isEmpty()) {
          try {
            chooseSupplier();
          } catch (IOException ignored) {}
        }
      }
    });

    /* Initialize from date filter */
    from_date.valueProperty().addListener((observableValue, localDate, t1) -> date.setValue(t1));

    /* Calculation event for every input on kilo, less, and price text area/field. */
    kilo_area.setOnKeyTyped(keyEvent -> computeCurrent());
    less_area.setOnKeyTyped(keyEvent -> computeCurrent());
    price_field.setOnKeyTyped(keyEvent -> computeCurrent());

    /* Put edit and delete buttons to default */
    setToNoSelectedItem();
    setToNoSelectedItem();

    // set search image icon

    // search_btn.setStyle("-fx-background-image: url('/img/search-icon.png')");
    // price_field.textProperty().addListener(new ChangeListener<String>() {
    // @Override
    // public void changed(
    // ObservableValue<? extends String> observable,
    // String oldValue, String newValue) {
    // if (!newValue.matches("^(0|[1-9]\\d*)(\\.\\d+)?$")) {
    // price_field.setText(newValue.replaceAll("[^\\d]", ""));
    // }
    // }
    // });


    userField.setText(LoginSession.userLast + ", " + LoginSession.userFirst);
  }

  private void computeCurrent() {
    double price = 0;
    double grossVal = 0;
    double lessVal = 0;

    try {
      grossVal =  Entry.getTotalWeight(Entry.parseWeight(getWeight("kilo_area")));
    } catch (IOException | NumberFormatException ignore) {}

    try {
      lessVal =  Entry.getTotalWeight(Entry.parseWeight(getWeight("less_area")));
    } catch (IOException | NumberFormatException ignore) {}

    try {
      price = Double.parseDouble(price_field.getText().trim());
    } catch (NumberFormatException ignore) {}

    double net = grossVal - lessVal;
    total_weight.setText(String.format(FORMATTER, net));
    total_amount.setText(String.format(FORMATTER, net * price));
  }

  @FXML
  private void filter() throws IOException {
    String from = getDateFormat(from_date.getValue());
    String to = getDateFormat(to_date.getValue());

    String supplier = sup_name.getText().trim();
    if (supplier.isEmpty()) {
      sup_name.setStyle(App.INVALID);
      App.showErrorAlert("Please select supplier", null);
    } else {
      sup_name.setStyle(App.WHITE);
      retrieveEntries(from, to, (to == null) ? new String[] { supplier, from } : new String[] { supplier, from, to });
    }
  }

  private ArrayList<Record> getRecords() {
    var dateData = new Object() {
      Set<LocalDate> stringList = new HashSet<>();
    };

    data.forEach(e -> {
      dateData.stringList.add(e.getLocalDate());
    });
    var temp = new Object() {
      ArrayList<Record> records = new ArrayList<>();
    };

    var supplier_temp = new Object() {
      final String sup_val = sup_name.getText();
    };

    dateData.stringList.forEach(a -> {
      ArrayList<Entry> entries = (ArrayList) data.stream().filter(b -> b.getLocalDate().equals(a))
          .collect(Collectors.toList());

      entries.forEach(e -> {
        SimpleDateFormat source = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat longFmt = new SimpleDateFormat("MMM dd/yyyy");

        try {
          e.setDate(longFmt.format(source.parse(e.getDate())));
        } catch (ParseException ignore) {}
      });

      /* Get advances and add to entries */
//      try {
//        ResultSet res = App.querySelect(String.format("select amount, type from advance where record_id = any (select record_id from record where supplier_id = '%s' and date = '%s');",
//            supplier_temp.sup_val, getDateSql(a))
//        );
//
//        while (res.next()) {
//          String type = res.getString("type").equals("a") ? "Advance" : "Fare";
//          entries.add(new Entry( type, -res.getDouble("amount")));
//        }
//      } catch (SQLException ignore) {}

      temp.records.add(new Record(a, entries));
    });

    temp.records.sort(new Comparator<Record>() {
      public int compare(Record rec1, Record rec2) {
        return rec1.getDate().compareTo(rec2.getDate());
      }
    });
    return temp.records;
  }

  @FXML
  private void export() throws IOException, SQLException {
    if (table.getItems().size() == 0) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("Nothing to print");
      alert.setContentText("Please make sure to have at least one entry");
      alert.showAndWait();
      return;
    }

    String supplier = sup_name.getText();
    ResultSet res = App.querySelect("select supplier_name from supplier where supplier_id = '" + supplier + "';");
    res.next();
    supplier = res.getString(1);
    ArrayList<Record> recordList = getRecords();
    String from = recordList.get(0).getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    String to = recordList.get(recordList.size() - 1).getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    String range = (from.equals(to)) ? from : String.format("%s to %s", from, to);
    String fileName = String.format(D_SB_BUSINESS_REPORTS_S_S_DOCX, supplier, range);

    try (XWPFDocument doc = new XWPFDocument()) {
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        final BigInteger margin = BigInteger.valueOf(MARGIN);
        pageMar.setLeft(margin);
        pageMar.setTop(margin);
        pageMar.setRight(margin);
        pageMar.setBottom(margin);
      // create a paragraph
      XWPFParagraph p1 = doc.createParagraph();
      p1.setAlignment(ParagraphAlignment.LEFT);

      // set font
      XWPFRun r1 = p1.createRun();
      r1.setFontSize(12);
//      r1.setText(String.format("Supplier: %s", supplier));
      r1.setText(supplier);
      r1.addBreak();
      r1.setText(range);

      XWPFTable docTable = doc.createTable();
      docTable.setWidth("100%");
      docTable.setCellMargins(0, 50, 0, 50);

      // Creating first row
      XWPFTableRow row1 = docTable.getRow(0);
      row1.getCell(0).setText("Date");
      XWPFParagraph pr0 = row1.getCell(0).getParagraphs().get(0);
      pr0.setAlignment(ParagraphAlignment.CENTER);
      pr0.getRuns().get(0).setBold(true);
      pr0.setSpacingAfter(0);
      row1.addNewTableCell().setText("Size");
      XWPFParagraph pr1 = row1.getCell(1).getParagraphs().get(0);
      pr1.getRuns().get(0).setBold(true);
      pr1.setAlignment(ParagraphAlignment.CENTER);
      pr1.setSpacingAfter(0);
      
      row1.addNewTableCell().setText("Gross");
      XWPFParagraph pr2 = row1.getCell(2).getParagraphs().get(0);
      pr2.getRuns().get(0).setBold(true);
      pr2.setAlignment(ParagraphAlignment.CENTER);
      pr2.setSpacingAfter(0);
      
      row1.addNewTableCell().setText("Less");
      XWPFParagraph pr3 = row1.getCell(3).getParagraphs().get(0);
      pr3.getRuns().get(0).setBold(true);
      pr3.setAlignment(ParagraphAlignment.CENTER);
      pr3.setSpacingAfter(0);
      
      row1.addNewTableCell().setText("Net");
      XWPFParagraph pr4 = row1.getCell(4).getParagraphs().get(0);
      pr4.getRuns().get(0).setBold(true);
      pr4.setAlignment(ParagraphAlignment.CENTER);
      pr4.setSpacingAfter(0);
      
      row1.addNewTableCell().setText("Price");
      XWPFParagraph pr5 = row1.getCell(5).getParagraphs().get(0);
      pr5.getRuns().get(0).setBold(true);
      pr5.setAlignment(ParagraphAlignment.CENTER);
      pr5.setSpacingAfter(0);
      
      row1.addNewTableCell().setText("Total");
      XWPFParagraph pr6 = row1.getCell(6).getParagraphs().get(0);
      pr6.getRuns().get(0).setBold(true);
      pr6.setAlignment(ParagraphAlignment.CENTER);
      pr6.setSpacingAfter(0);
      // End row

      // Initialize column width
      final int[] cols = {
        1500, 1300,
        1800, 1800,
        1300, 1300,
        1300
      };

      var dateObject = new Object() {
        String date = null;
      };

      ArrayList<Double> totalAmount = new ArrayList<>();
      recordList.forEach(recordItem -> {
        recordItem.getEntries().forEach(e -> {
          XWPFTableRow row = docTable.createRow();
          String currentDate = e.getDate();
          Double currentAmount = e.getTotalAmount();

          if (dateObject.date == null) {
            dateObject.date = currentDate;
          } else {
            if (dateObject.date.equals(currentDate)) {
              currentDate = "";
            } else {
              dateObject.date = currentDate;
            }
          }

          XWPFTableCell cell0 = row.getCell(0);
          cell0.setText(currentDate);
          XWPFParagraph ph0 = cell0.getParagraphs().get(0);
          ph0.setAlignment(ParagraphAlignment.CENTER);
          ph0.setSpacingAfter(0);
          CTTblWidth cellWidth = cell0.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[0]));

          XWPFTableCell cell1 = row.getCell(1);
          cell1.setText(e.getSize());
          XWPFParagraph ph1 = cell1.getParagraphs().get(0);
          // ph1.setAlignment(ParagraphAlignment.CENTER);
          ph1.setSpacingAfter(0);
          cellWidth = cell1.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[1]));

          XWPFTableCell cell2 = row.getCell(2);
          cell2.setText(e.getGross());
          XWPFParagraph ph2 = cell2.getParagraphs().get(0);
          // ph2.setAlignment(ParagraphAlignment.CENTER);
          ph2.setSpacingAfter(0);
          cellWidth = cell2.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[2]));

          XWPFTableCell cell3 = row.getCell(3);
          cell3.setText(e.getLess());
          XWPFParagraph ph3 = cell3.getParagraphs().get(0);
          // ph3.setAlignment(ParagraphAlignment.CENTER);
          ph3.setSpacingAfter(0);
          cellWidth = cell3.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[3]));

          XWPFTableCell cell4 = row.getCell(4);
          cell4.setText(String.format("%,.2f", e.getTotalWeight()));
          XWPFParagraph ph4 = cell4.getParagraphs().get(0);
          ph4.setAlignment(ParagraphAlignment.RIGHT);
          ph4.setSpacingAfter(0);
          cellWidth = cell4.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[4]));

          XWPFTableCell cell5 = row.getCell(5);
          cell5.setText(String.format("%,.2f", e.getPrice()));
          XWPFParagraph ph5 = cell5.getParagraphs().get(0);
          ph5.setAlignment(ParagraphAlignment.RIGHT);
          ph5.setSpacingAfter(0);
          cellWidth = cell5.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[5]));

          XWPFTableCell cell6 = row.getCell(6);
          cell6.setText(String.format("%,.2f", currentAmount));
          XWPFParagraph ph6 = cell6.getParagraphs().get(0);
          ph6.setAlignment(ParagraphAlignment.RIGHT);
          ph6.setSpacingAfter(0);
          cellWidth = cell6.getCTTc().addNewTcPr().addNewTcW();
          cellWidth.setW(BigInteger.valueOf(cols[6]));
        });
        
        Double recordAmount = recordItem.getTotalAmountOfRecord();
        totalAmount.add(recordAmount);

        /* Adds subtotal for every date */
        if (recordItem.getEntries().size() > 1) {
            XWPFTableRow row = docTable.createRow();
            XWPFTableCell cell = row.getCell(6);
            cell.setText(String.format("%, .2f", recordAmount));
            XWPFParagraph subTotalLabelPar = cell.getParagraphs().get(0);
            subTotalLabelPar.setAlignment(ParagraphAlignment.RIGHT);
            subTotalLabelPar.setSpacingAfter(0);
            cell.setColor("d3d3d3");

            CTHMerge hMerge = CTHMerge.Factory.newInstance();
            hMerge.setVal(STMerge.RESTART);
            XWPFTableCell cell0 = row.getCell(0);
            cell0.getCTTc().addNewTcPr().setHMerge(hMerge);
            hMerge.setVal(STMerge.CONTINUE);
            XWPFTableCell cell1 = row.getCell(1);
            cell1.getCTTc().addNewTcPr().setHMerge(hMerge);
            XWPFTableCell cell2 = row.getCell(2);
            cell2.getCTTc().addNewTcPr().setHMerge(hMerge);
            XWPFTableCell cell3 = row.getCell(3);
            cell3.getCTTc().addNewTcPr().setHMerge(hMerge);
            XWPFTableCell cell4 = row.getCell(4);
            cell4.getCTTc().addNewTcPr().setHMerge(hMerge);
            XWPFTableCell cell5 = row.getCell(5);
            cell5.getCTTc().addNewTcPr().setHMerge(hMerge);
            cell0.setText("Sub total");
            XWPFParagraph subtotalPar = cell0.getParagraphs().get(0);
            subtotalPar.setAlignment(ParagraphAlignment.RIGHT);
            subtotalPar.setSpacingAfter(0);
        }
      });

      XWPFParagraph summaryParagraph = doc.createParagraph();
      summaryParagraph.setSpacingAfter(0);
      XWPFRun r2 = summaryParagraph.createRun();
      r2.setBold(true);
      r2.addBreak();
      r2.setText(String.format("%nSummary:")) ;
      r2.setBold(false);
      r2.addBreak();

      // var sumObject = new Object () { String summary = ""; };
      recordList.forEach(e -> {
        // ArrayList<String[]> enSumList = e.getSummary();
        r2.setText(String.format("%n%s - %,-12.2f", e.getDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")), e.getTotalAmountOfRecord()));
        r2.addBreak();

        // Iterator<String[]> it = enSumList.iterator();

        // while(it.hasNext()) {
        //   String[] temp = it.next();
        //   r2.setText(String.format("\t%6s x%10s =%15s%n", temp[0], temp[1], temp[2]));
        //   r2.addBreak();
        // }
        // r2.setText(String.format("%nSub Total = %s%n", e.getTotalAmountOfRecord()));
        // r2.addBreak();
      });
      // r2.setText(sumObject.summary);


      XWPFParagraph totalParagraph = doc.createParagraph();
      XWPFRun r3 = totalParagraph.createRun();
      r3.setBold(true);
      r3.setText(String.format("Total: %, .2f", totalAmount.stream().mapToDouble(Double::doubleValue).sum()));

      // save it to .docx file
      try (FileOutputStream out = new FileOutputStream(fileName)) {
        doc.write(out);
        try {
          // constructor of file class having file as argument
          File file = new File(fileName);
          // check if Desktop is supported by Platform or not
          if(!Desktop.isDesktopSupported()) {
            System.out.println("not supported");
            return;
          }
          Desktop desktop = Desktop.getDesktop();
          if(file.exists())         //checks file exists or not
            desktop.open(file);              //opens the specified file
        } catch (Exception ignore) {}
      }
    }
  }

  private void clearFields() {
    saveEntry_btn.setDisable(true);
    enter_btn.setDisable(false);
    edit_btn.setDisable(false);
    date.setDisable(false);
    kilo_area.clear();
    less_area.clear();
    price_field.clear();
    total_weight.clear();
    total_amount.clear();
    clearSizeCBxs();
  }

  private void clearSizeCBxs() {
    sz_small.setSelected(false);
    sz_assorted.setSelected(false);
    sz_medium.setSelected(false);
    sz_jumbo.setSelected(false);
    sz_others.setSelected(false);
    sz_reject.setSelected(false);
    sz_otherFld.clear();
  }

  /**
   * Sets edit and delete buttons to non-clickable
   */
  private void setToNoSelectedItem() {
    delete_btn.setDisable(true);
    edit_btn.setDisable(true);
  }

  private int getRecordId(String supplier, LocalDate date) {
    try {
      System.out.println(String.format("select record_id from record where supplier_id = '%s' and date = '%s';",   supplier, getDateSql(date)));
      ResultSet rs = App.querySelect(String.format("select record_id from record where supplier_id = '%s' and date = '%s';",   supplier, getDateSql(date)));
      if (rs.next()) {
        return rs.getInt("record_id");
      }
    } catch (SQLException ignore) {}
    return 0;
  }

  private void retrieveEntries(String from, String to, String[] param) {
    try {
      final String condition = (to == null) ? "= ?" : "between ? and ?";
      final String dataType = (to == null) ? "st" : "stt";
      final String getEntriesQry = String.format(
          "select entry_id, size, gross, less, net, price, total, date from record_entry join record using " +
                  "(record_id) where record_id = any (select record_id " +
                  "from record where supplier_id = ? and date %s);",
          condition);

      ResultSet result = App.preparedSelect(getEntriesQry, param, dataType);
      data.clear();
      String supplier = sup_name.getText();
      Set<LocalDate> currentLDList = new HashSet<>();
      while (result.next()) {
        LocalDate date = result.getDate("date").toLocalDate();
        currentLDList.add(date);
        data.add(new Entry(
            result.getInt("entry_id"), date,
            result.getString("gross"), result.getString("less"),
            result.getString("size"), result.getDouble("price"),
            result.getDouble("net"), result.getDouble("total")));
      }

      currentLDList.forEach(ld -> {
        try {
          ResultSet rs = App.querySelect(String.format(
              "select amount, type from advance where record_id = '%s';", getRecordId(supplier, ld)
          ));

          while (rs.next()) {
            Entry entry = new Entry(rs.getString("type"), -rs.getDouble("amount"));
            entry.setLocalDate(ld);
            data.add(entry);
          }
        } catch (SQLException ignore) {}
      });
    } catch (SQLException ignored) {}
  }

  @FXML
  private void logout() throws IOException {
    ButtonType logoutBtn = new ButtonType("Logout", ButtonBar.ButtonData.YES);
    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you're going to log out?",
            logoutBtn, cancelBtn);
    alert.setHeaderText("Logout Confirmation");
    alert.setTitle("Warning");
    Optional<ButtonType> result = alert.showAndWait();

    if (result.get() == logoutBtn) {
      Stage stage = new Stage();
      Scene scene = new Scene(App.loadFXML("login"), 1140, 565);
      stage.setScene(scene);
      stage.setResizable(false);
      stage.show();

      ((Stage) search_btn.getScene().getWindow()).close();
      LoginSession.logout();
    }
  }

  @FXML
  private void showReports() throws IOException {
    Stage stage = new Stage();
    stage.setTitle("Reports");
    stage.setScene(new Scene(App.loadFXML("reports"), 596, 403));
    stage.setResizable(false);
    stage.show();
  }
}
