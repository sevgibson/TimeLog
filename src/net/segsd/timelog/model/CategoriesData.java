package net.segsd.timelog.model;

import net.segsd.timelog.util.Util;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class CategoriesData extends Hashtable<String,CategoryData> {
  private final static long serialVersionUID = 1;
  private File dataDir;
  public CategoriesData(File dataDir) {
    if (!dataDir.exists())
      if (!dataDir.mkdirs()) {
        System.err.println("Error making data directory: "+dataDir);
      }
    this.dataDir = dataDir;
    load();
  }
  
  public void addCategory(String categoryName) {
    this.put(categoryName,new CategoryData(categoryName));
  }
  
  public Vector<String> getCategoryNames() {
    Vector<String> names = new Vector<String>();
    for (String categoryName : this.keySet()) {
      names.addElement(categoryName);
    }
    return names;
  }
  
  public CategoryData getCategory(String categoryName) {
    return this.containsKey(categoryName) ? this.get(categoryName) : null;
  }
  protected CategoryData getCategory(String categoryName, boolean create) {
    if (!this.containsKey(categoryName) && create)
      addCategory(categoryName);
    return getCategory(categoryName);
  }

  public void addEntry(String categoryName, EntryData entry) {
    getCategory(categoryName, true).addEntry(entry);
  }
  
  public String toString() {
    Vector<String> out = new Vector<String>();
    for (String categoryName : getCategoryNames()) {
      out.addElement(getCategory(categoryName).toString());
    }
    return Util.join("\n", out);
  }
  
  public void save() {
    if (dataDir.exists() && dataDir.isDirectory()) {
      // Need to get the list of all dates, and write them all out...
      Hashtable<String,Integer> dates = new Hashtable<String,Integer>();
      for (String categoryName : this.keySet()) {
        for (String date : getCategory(categoryName).getDates()) {
          if (!categoryName.startsWith("--"))
            dates.put(date, 0);
        }
      }
      for (String date : dates.keySet()) {
        System.err.println("Writing file: "+date);
        try {
          ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(dataDir, date+".data")));
          for (String categoryName : this.keySet()) {
            if (!categoryName.startsWith("--") &&
                getCategory(categoryName).hasDate(date)) {
              out.writeUTF(categoryName);
              getCategory(categoryName).save(out, date);
            }
          }
          out.writeUTF("-- done --");
          out.close();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
  }
  private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  public void load() {
    if (dataDir.exists() && dataDir.isDirectory()) {
      String date = dateFormat.format(new Date());
      File file = new File(dataDir, date+".data");
      if (file.exists()) {
        try {
          ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
          boolean done = false;
          while (!done) {
            String categoryName = in.readUTF();
            if (categoryName.equals("-- done --"))
              done = true;
            else
              getCategory(categoryName, true).load(in, date);
          }
        } catch (IOException ex) {
          ex.printStackTrace();
          System.exit(1);
        }
      }
    }
    System.err.println("Read data:\n"+toString());
  }
}