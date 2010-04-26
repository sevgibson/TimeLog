package net.segsd.timelog.view;

import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import net.segsd.timelog.util.AutoGridLayout;
import net.segsd.timelog.util.GuiUtil;
import net.segsd.timelog.model.EntryData;
import net.segsd.timelog.model.CategoriesData;

public class TimeLogFrame extends JFrame {
  private final static long serialVersionUID = 1;
  
  private String selected;
  private Date start = now();

  private Color saveColor = Color.RED;
  private Color selectedColor = Color.CYAN;
  private DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
  private DateFormat timeFormat = SimpleDateFormat.getTimeInstance();
  private long delay = 500;
  private TimerTask timerTask;
  private Timer timer;
  private JLabel dateTimeLabel = new JLabel();
//  private JLabel timeLabel = new JLabel();
  private JButton newCatButton = new JButton("Add New Category");
  private JTextField newCatField = new JTextField();

  private Vector<String> categoryNames = new Vector<String>();
  private Vector<JButton> categoryButtons = new Vector<JButton>();
  private Vector<JLabel> categoryLabels = new Vector<JLabel>();
//  private DefaultComboBoxModel categoryModel = new DefaultComboBoxModel();
//  private JComboBox categoryBox = new JComboBox(categoryMaodel);
  private JPanel panel = new JPanel();
  private AutoGridLayout agl = new AutoGridLayout();
  private CategoriesData categories;
  
  public TimeLogFrame() {
    categories = new CategoriesData(new File("data"));
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        saveData();
        System.exit(0);
      }
    });
    categoryNames.addElement(selected = "-- timer off --");
    categoryNames.addElement("PD");
    categoryNames.addElement("SSM");
    categoryNames.addElement("Kontact");
//    categoryBox.setEditable(true);
//    group.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent ae) {
//        if (!selected.equals(categoryModel.getSelectedItem())) {
////          System.err.println("Action: "+ae.getActionCommand());
//          saveData();
//          selected = (String) categoryModel.getSelectedItem();
//        }
//      }
//    });
    setTitle("TimeLog");
    this.setContentPane(panel);
    panel.setLayout(agl);
    dateTimeLabel.setHorizontalAlignment(JLabel.CENTER);
    panel.add(dateTimeLabel, new int[] { 0, 0, 2, 1 });
//    timeLabel.setHorizontalAlignment(JLabel.CENTER);
//    panel.add(timeLabel, new int[] { 1, 0 });
//    newCatButton.setHorizontalAlignment(JButton.CENTER);
    panel.add(newCatField, new int[] { 0, 1 });
    panel.add(newCatButton, new int[] { 1, 1 });
    int i = 2;
    for (String categoryName : categoryNames) {
      JButton categoryButton = new JButton(categoryName);
      JLabel categoryLabel = new JLabel("");
      categoryLabel.setHorizontalAlignment(JLabel.RIGHT);
      if (saveColor == Color.RED) saveColor = categoryButton.getBackground();
      if (selected.equals(categoryName)) {
        categoryButton.setBackground(selectedColor);
      }
      categoryButtons.addElement(categoryButton);
      categoryLabels.addElement(categoryLabel);
      categoryLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
      categoryButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (!selected.equals(ae.getActionCommand())) {
            int i = categoryNames.indexOf(selected);
            categoryButtons.elementAt(i).setBackground(saveColor);
            saveData();
            selected = ae.getActionCommand();
            i = categoryNames.indexOf(selected);
            categoryButtons.elementAt(i).setBackground(selectedColor);
          }
        }
      });
      panel.add(categoryButton, new int[] { 0, i });
      panel.add(categoryLabel, new int[] { 1, i });
      i++;
    }
    setDateTime();
    this.pack();
    GuiUtil.center(this);
    timerTask = new TimerTask() {
      public void run() {
        // Display the current time...
        setDateTime();
      }
    };
    timer = new Timer();
    timer.schedule(timerTask, delay, delay);
  }
  private void setDateTime() {
    dateTimeLabel.setText("Date: "+date()+"  Time: "+time());
    dateTimeLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
    updateTotals();
  }
  private synchronized void saveData() {
    Date s = start;
    Date e = (start = now());
    categories.addEntry(selected, new EntryData(s, e, "saving data"));
//    System.err.println(dataToString());
    categories.save();
  }
  private String dataToString() {
    return categories.toString();
  }
  private Date now() {
    return new Date();
  }
  private String date() {
    return dateFormat.format(now().getTime());
  }
  private String time() {
    return timeFormat.format(now().getTime());
  }
  private void updateTotals() {
    for (int i = 0; i < categoryNames.size(); i++) {
      long total = 0;
      if (categories.getCategory(categoryNames.elementAt(i)) != null)
        total = categories.getCategory(categoryNames.elementAt(i)).getTotalToday();
      if (categoryNames.elementAt(i).equals(selected))
        total += now().getTime() - start.getTime();
      total /= 1000;
      long hours = total / 3600;
      long minutes = (total - hours * 3600) / 60;
      long seconds = (total - hours * 3600 - minutes * 60);
      DecimalFormat df = new DecimalFormat("00");
      categoryLabels.elementAt(i).setText(df.format(hours)+":"+df.format(minutes)+":"+df.format(seconds));
    }
  }
}