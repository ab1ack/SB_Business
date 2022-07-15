package com.mycompany.sb_business;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Entry {
    private String date;
    private LocalDate ldate;
    private Integer entryId;
    private String gross;
    private String less;
    private String size;
    private Double price;
    private Double advance;
    private Double fare;
    private Double totalWeight;
    private Double totalAmount;

    public Entry(LocalDate dateEntered, List<String> gross, List<String> less, Double price, String size) {
        this.date = dateEntered.format(DateTimeFormatter.ofPattern("MM-dd-YYYY"));
        this.ldate = dateEntered;
        this.entryId = null;
        this.price = price;
        this.size = size == null ? "" : size;

        this.gross = String.join(", ", gross);
        this.less = String.join(", ", less);
        this.totalWeight =  Entry.getTotalWeight(Entry.parseWeight(gross)) -  Entry.getTotalWeight(Entry.parseWeight(less));
        this.totalAmount = this.totalWeight * price;
    }

    public Entry(String gross, String less, String size, Double price, Double totalWeight, Double totalAmount) {
        this.entryId = null;
        this.gross = gross;
        this.less = less;
        this.size = size == null ? "" : size;
        this.price = price;
        this.totalWeight = totalWeight;
        this.totalAmount = totalAmount;
    }

    public Entry(int entry_id, LocalDate date, List<String> gross, List<String> less, String size, Double price, Double totalWeight, Double totalAmount) {
        this.entryId = entry_id;
        this.ldate = date;
        this.date = date.format(DateTimeFormatter.ofPattern("MM-dd-YYYY"));
        this.gross = String.join(", ", gross);
        this.less = String.join(", ", less);
        this.size = size == null ? "" : size;
        this.price = price;
        this.totalWeight = totalWeight;
        this.totalAmount = totalAmount;
    }
    
    public Entry(int entry_id, LocalDate date, String gross, String less, String size, Double price, Double totalWeight, Double totalAmount) {
        this.entryId = entry_id;
        this.ldate = date;
        this.date = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.gross = gross;
        this.less = less;
        this.size = size == null ? "" : size;
        this.price = price;
        this.totalWeight = totalWeight;
        this.totalAmount = totalAmount;
    }

    public Entry(String type, Double advance) {
        this.entryId = null;
        this.gross = "";
        this.less = "";
        this.size = type.equals("a") ? "Advance" : "Fare";
        this.price = 0.0;
        this.totalWeight = 0.0;
        this.totalAmount = advance;
    }
    
    public LocalDate getLocalDate() {
        return ldate;
    }

    public void setLocalDate(LocalDate date) {
        this.date = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.ldate = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getEntryId() {
        return entryId;
    }

    public void setEntryId(int entry_id) {
        this.entryId = entry_id;
    }
    
    protected String getDeleteQry(boolean isAdvance, int record_id, Character type) {
        return isAdvance ? String.format("delete from advance where record_id = '%d' and type = '%s';", record_id, type)
            : String.format("delete from record_entry where entry_id = %d", this.entryId);
    }
    protected String getQryValue(int recordId) {
        return String.format("insert into record_entry (record_id, size, gross, less, net, price, total) values (%d, '%s', '%s', '%s', %f, %f, %f);", recordId, this.size, this.gross, this.less, this.totalWeight, this.price, this.totalAmount);
    }
    
    protected String getUpdateQry() {
        return String.format("update record_entry set size = '%s', gross = '%s', less = '%s', net = %f, price = %f, total = %f where entry_id = %d;", this.size, this.gross, this.less, this.totalWeight, this.price, this.totalAmount, this.entryId);
    }

    public static List<Float> parseWeight(List<String> data) {
        System.out.println(data.size());
        return data.stream().map(Float::parseFloat).collect(Collectors.toList());
    }

    public static Double getTotalWeight(List<Float> weights) {
        return weights.stream().mapToDouble(Float::doubleValue).sum();
    }
    
    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    public String getLess() {
        return less;
    }

    public void setLess(String less) {
        this.less = less;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double total_weight) {
        this.totalWeight = total_weight;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double total_amount) {
        this.totalAmount = total_amount;
    }
    
    public String getTAreaValue(String data) {
        String[] values = data.replaceAll(" ", "").split(",");
        return String.join("\n", values);
    }

    public Double getAdvance() {
        return advance;
    }

    public void setAdvance(Double advance) {
        this.advance = advance;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    @Override
    public String toString() {
        return String.format("Id: %d\nSize:%s \nGross - Less: %s - %s\nNet: %,.2f\n Price: %,.2f\n Total Amount: %,.2f", 
            this.entryId, this.size, this.gross, 
            this.less, this.totalWeight, this.price, this.totalAmount);
    }
    
    @Override
    public boolean equals(Object other) {
        Entry otherEn = (Entry) other;
        return this.entryId == otherEn.getEntryId() && 
                this.size == otherEn.getSize() && 
                this.gross == otherEn.getGross() &&
                this.less == otherEn.getLess() &&
                this.totalWeight == otherEn.getTotalWeight() &&
                this.price == otherEn.getPrice() &&
                this.totalAmount == otherEn.getTotalAmount();
    }

    public String getGrossTAValue() {
        return getTAreaValue(this.gross);
    }
    
    public String getLessTAValue() {
        return getTAreaValue(this.less);
    }
//
//    public String getAdvQry(Integer recordId, String type) {
//        return String.format("insert into advance (record_id, amount, type) values (%d, %f, '%s');", recordId, ((type.equals("a") ? this.advance : this.fare)), type);
//    }
//    public String getUpdAdvQry(Integer advId, String type) {
//        return String.format("update advance set amount = '%f' where id = '%d';", ((type.equals("a")) ? this.advance : this.fare), advId);
//    }
}
