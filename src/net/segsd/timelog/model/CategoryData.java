package net.segsd.timelog.model;

import java.util.Date;
import java.util.Vector;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import net.segsd.timelog.util.Util;

public class CategoryData {
  private String categoryName;
  private Hashtable<String,EntriesData> dateLogs;
  public CategoryData(String category) {
    this.categoryName = category;
    setDateLogs(new Hashtable<String, EntriesData>());
  }
  public String getCategory() {
    return categoryName;
  }
  public void setCategory(String category) {
    this.categoryName = category;
  }
  public Hashtable<String, EntriesData> getDateLogs() {
    return dateLogs;
  }
  protected void setDateLogs(Hashtable<String, EntriesData> dateLogs) {
    this.dateLogs = dateLogs;
  }
  public EntriesData getDateLog(String date) {
    EntriesData entries;
    if (!dateLogs.containsKey(date))
      dateLogs.put(date, entries = new EntriesData());
    else 
      entries = dateLogs.get(date);
    return entries;
  }
  public Vector<String> getDates() {
    return new Vector<String>(dateLogs.keySet());
  }
  private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  public static String getToday() {
    return dateFormat.format(new Date());
  }
  public void addEntry(EntryData entry) {
    String date = dateFormat.format(entry.getStart());
//    System.err.println("Adding entry to "+categoryName+" "+date+": "+entry);
    getDateLog(date).addEntry(entry);
  }
  
  public String toString() {
    Vector<String> out = new Vector<String>();
    for (String date : dateLogs.keySet()) {
      out.addElement("date: "+date+"\n    "+dateLogs.get(date).toString());
    }
    return categoryName+"\n  "+Util.join("\n   ", out);
  }
  public boolean hasDate(String date) {
    return dateLogs.containsKey(date);
  }
  
  public void save(ObjectOutputStream out, String date) throws IOException {
    getDateLog(date).save(out);
  }
  public void load(ObjectInputStream in, String date) throws IOException {
    getDateLog(date).load(in);
  }
  public long getTotalToday() {
    return this.getDateLog(getToday()).getTotal();
  }
}
