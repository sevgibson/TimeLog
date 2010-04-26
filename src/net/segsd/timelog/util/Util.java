package net.segsd.timelog.util;

/**
 * <p>Title: Generic Reusable Utilities</p>
 * <p>Description: A set of classes which are very generic and reusable.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company:  Scott Everett Gibson Software Development</p>
 * @author Scott Gibson
 * @version 1.0
 */

import java.util.*;

public class Util {
  public static String join(String join, List values) {
    return join(join, values.toArray());
  }
  public static String join(String join, Vector values) {
    return join(join, values.toArray());
  }
  public static String join(String join, Object values[]) {
    String r = "";
    for (int i = 0; i < values.length; i++)
      r += (i == 0 ? "" : join)+values[i];
    return r;
  }
  public static String[] grep(String pattern, String[] values) {
    Vector<String> out = new Vector<String>();
    for (String s : values) {
      if (s.matches(pattern))
        out.add(s);
    }
    return out.toArray(new String[0]);
  }
  public static List<String> grep(String pattern, List<String> values) {
    Vector<String> out = new Vector<String>();
    for (String s : values) {
      if (s.matches(pattern)) out.add(s);
    }
    return out;
  }
  public static String[] split(String split, String string) {
    Vector<String> s = new Vector<String>();
    int i = 0;
    int n;
    while ((n = string.indexOf(split, i)) >= 0) {
      s.addElement(string.substring(i,n));
      i = n + split.length();
    }
    return s.toArray(new String[0]);
  }
  public static Object[] addEntry(Object[] entries, Object entry) {
    int oldLength = (entries == null) ? 0 : entries.length;
    Object[] newEntries = (Object[]) java.lang.reflect.Array.newInstance(entry.getClass(), oldLength + 1); 
    for (int i = 0; i < oldLength; i++) {
      newEntries[i] = entries[i];
    }
    newEntries[oldLength] = entry;
    return newEntries;
  }
}
