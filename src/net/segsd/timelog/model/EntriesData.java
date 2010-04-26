package net.segsd.timelog.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import net.segsd.timelog.util.Util;

public class EntriesData extends Vector<EntryData> {
  private final static long serialVersionUID = 1;
  public EntriesData() {
    
  }
  public void addEntry(EntryData entry) {
    this.addElement(entry);
    total += entry.getEnd().getTime() - entry.getStart().getTime();
  }
  private long total = 0;
  public long getTotal() {
    return total;
  }
  public String toString() {
    Vector<String> out = new Vector<String>();
    for (EntryData entry : this) {
      out.addElement(entry.toString());
    }
    return Util.join("\n    ", out);
  }
  public void save(ObjectOutputStream out) throws IOException {
    out.writeInt(this.size());
    for (EntryData entry : this) {
      entry.save(out);
    }
  }
  public void load(ObjectInputStream in) throws IOException {
    int i = in.readInt();
    while (i-- > 0) this.addEntry(EntryData.load(in));
  }
}
