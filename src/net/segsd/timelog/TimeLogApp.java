package net.segsd.timelog;

import net.segsd.timelog.view.TimeLogFrame;

public class TimeLogApp {
  
  public TimeLogApp() {
    // Set up:
    //  - time log file
    //  - create frame
    //  - display frame
    //  - monitor for exit...
    TimeLogFrame tlf = new TimeLogFrame();
    tlf.setVisible(true);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new TimeLogApp();
  }

}
