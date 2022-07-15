/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sb_business;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class Record {
    private String supplier_id;
    private String supplier;
    private LocalDate date;
    private ArrayList<Entry> entries;
    private Double advance;

    public Record(String supplier_id, String supplier, LocalDate date, Double advance) {
        this.supplier_id = supplier_id;
        this.supplier = supplier;
        this.date = date;
        this.advance = advance;
    }

    public Record(String supplier_id, LocalDate date, ArrayList<Entry> entries) {
        this.supplier_id = supplier_id;
        this.supplier = supplier;
        this.date = date;
        this.entries = entries;
    }

    public Record(LocalDate date, ArrayList<Entry> entries) {
        this.date = date;
        this.entries = entries;
    }
    
    public Double getTotalAmountOfRecord() {
        return this.entries.stream().mapToDouble(a -> a.getTotalAmount()).sum();
    }
   
    public ArrayList<Entry> getEntriesToUpdate() {
        
        return (ArrayList<Entry>) this.entries.stream()
                .filter(e ->  e.getEntryId() != null).collect(Collectors.toList());
    }
    
    public String getInsertEntriesQry(int recordEntryId) {
        String query = "insert into record_entry (record_id, size, gross, less, net, price, total) values ";
        ArrayList<String> entriesQry = new ArrayList<>();
        ArrayList<Entry> newEntries = (ArrayList<Entry>) entries.stream().filter(e -> e.getEntryId() == null).collect(Collectors.toList());
        newEntries.forEach(e -> {
            entriesQry.add(e.getQryValue(recordEntryId));
        });
        query += String.join(", ", entriesQry) + ";";
        return query;
    }

    public Double getAdvance() {
        return advance;
    }

    public void setAdvance(Double advance) {
        this.advance = advance;
    }

    public String getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id) {
        this.supplier_id = supplier_id;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    public ArrayList<String[]> getSummary() {
        ArrayList<String[]> temp = new ArrayList<> ();
        final String FORMAT = "%,.2f";
        entries.forEach(e -> {
            String[] entrySummary = new String[] {
                String.format(FORMAT, e.getTotalWeight()),
                String.format(FORMAT, e.getPrice()),
                String.format(FORMAT, e.getTotalAmount())
            };
            temp.add(entrySummary);
        });
        return temp; 
    }
    
    
}
