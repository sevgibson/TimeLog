package net.segsd.timelog.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EntryData {
  private Date start;
  private Date end;
  private String comment;
  private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  
  public EntryData(Date start, Date end, String comment) {
    this.start = start;
    this.end = end;
    this.comment = comment;
  }
  
  public Date getStart() {
    return start;
  }
  public Date getEnd() {
    return end;
  }
  public String getComment() {
    return comment;
  }
  public String toString() {
    return dateFormat.format(start)+" to "+dateFormat.format(end);
  }
  public void save(ObjectOutputStream out) throws IOException {
    out.writeLong(start.getTime());
    out.writeLong(end.getTime());
    out.writeUTF(comment);
  }
  public static EntryData load(ObjectInputStream in) throws IOException {
    Date start = new Date(in.readLong());
    Date end = new Date(in.readLong());
    String comment = in.readUTF();
    return new EntryData(start, end, comment);
  }
}
