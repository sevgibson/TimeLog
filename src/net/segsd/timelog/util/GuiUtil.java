package net.segsd.timelog.util;

/**
 * <p>Title: Generic Reusable Utilities</p>
 * <p>Description: A set of classes which are very generic and reusable.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company:  Scott Everett Gibson Software Development</p>
 * @author Scott Gibson
 * @version 1.0
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

public class GuiUtil {
  public static void setUIManager() {
    try {
      UIManager.LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
      String className = UIManager.getSystemLookAndFeelClassName();
      for (int i = 0; i < lfs.length; i++) {
        if (lfs[i].getClassName().indexOf("Windows") >= 0)
          className = lfs[i].getClassName();
      }
      try {
        UIManager.setLookAndFeel(className);
      } catch (Exception ex) {
        for (int i = 0; i < lfs.length; i++) {
          if (lfs[i].getClassName().indexOf("Motif") >= 0)
            className = lfs[i].getClassName();
        }
        UIManager.setLookAndFeel(className);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public static void upperLeft(Component c) {
    c.setLocation(0,0);
  }
  public static void lowerRight(Component c) {
    Dimension cSize = c.getSize();
    if (cSize == null || cSize.width == 0 || cSize.height == 0)
      cSize = c.getPreferredSize();
    Dimension pSize = Toolkit.getDefaultToolkit().getScreenSize();
    c.setLocation(pSize.width - cSize.width, pSize.height - cSize.height - 50);
  }
  public static void upperRight(Component c) {
    Dimension cSize = c.getSize();
    if (cSize == null || cSize.width == 0)
      cSize = c.getPreferredSize();
    Dimension pSize = Toolkit.getDefaultToolkit().getScreenSize();
    c.setLocation(pSize.width - cSize.width, 0);
  }
  public static void lowerLeft(Component c) {
    Dimension cSize = c.getSize();
    if (cSize == null || cSize.height == 0)
      cSize = c.getPreferredSize();
    Dimension pSize = Toolkit.getDefaultToolkit().getScreenSize();
    c.setLocation(0, pSize.height - cSize.height - 50);
  }
  public static void fullSizeNoDecorations(Frame c) {
    c.setUndecorated(true);
    Dimension pSize = Toolkit.getDefaultToolkit().getScreenSize();
    c.setPreferredSize(pSize);
    c.setSize(pSize);
  }
  public static void center(Component c) {
    center(c, null);
  }
  public static void center(Component c, Container p) {
    Dimension cSize = c.getSize();
    if (cSize == null || cSize.width == 0 || cSize.height == 0)
      cSize = c.getPreferredSize();
    Dimension pSize;
    Point loc;
    if (p != null) {
      loc = p.getLocationOnScreen();
//      loc = p.getLocation();
      pSize = p.getSize();
    } else {
      loc = new Point(0,0);
      pSize = Toolkit.getDefaultToolkit().getScreenSize();
    }
    c.setLocation((pSize.width - cSize.width) / 2 + loc.x, (pSize.height - cSize.height) / 2 + loc.y);
  }
  public static void findUnobscuredLocation(Component c, Container p) {
    // Where is the upper left point of the parent...
    Point screenLocation = p.getLocationOnScreen();
    int x = screenLocation.x;
    int y = screenLocation.y;
    // How large is the parent?
    Dimension parentSize = p.getSize();
    // How large will the component be?
    Dimension compSize = c.getSize();
    // How much space do we have on the screen?
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // Figure out how to show the component on the screen without covering up
    // the parent...

    // See if there is room below the parent?
    if (y + parentSize.height + parentSize.height <= screenSize.height) {
      c.setLocation(x+(parentSize.width/2)-compSize.width/2, y+parentSize.height);
    } else
     // Above the parent?
    if (compSize.height < y) {
      c.setLocation(x+(parentSize.width/2)-compSize.width/2, y-compSize.height);
    } else
    // Right of the parent?
    if (x + parentSize.width + compSize.width <= screenSize.width) {
      c.setLocation(x+parentSize.width, y+(parentSize.height/2)-compSize.height/2);
    } else
    // Left of the parent?
    if (compSize.width < x) {
      c.setLocation(x-compSize.width, y+(parentSize.height/2)-compSize.height/2);
    } else {
      // Settle for centering over the parent...
      GuiUtil.center(c, p);
    }
  }
  public static JFrame findFrameParent(JComponent component) {
    Component parent = component.getParent();
    while (parent != null) {
      if (parent instanceof JFrame) return (JFrame) parent;
      parent = parent.getParent();
    }
    return null;
  }
  public static JInternalFrame findIFrameParent(JComponent component) {
    Component parent = component.getParent();
    while (parent != null) {
      if (parent instanceof JInternalFrame) return (JInternalFrame) parent;
      parent = parent.getParent();
    }
    return null;
  }
  public static Component createDialog(JComponent parent, String title, JComponent component, boolean modal) {
    return createDialog(findFrameParent(parent), findIFrameParent(parent), title, component, modal);
  }
  public static Component createDialog(JFrame frame, JInternalFrame iframe, String title, JComponent component, boolean modal) {
    if (frame == null) {
      JInternalFrame dialog = new JInternalFrame(title, true,true,true);
      if (modal) dialog.setLayer(JDesktopPane.MODAL_LAYER);
      dialog.getContentPane().add(component);
      dialog.pack();
      GuiUtil.center(dialog, iframe);
      return dialog;
    } else {
      JDialog dialog = new JDialog(frame, title, modal);
      dialog.getContentPane().add(component);
      dialog.pack();
      GuiUtil.center(dialog, frame);
      return dialog;
    }
  }
  public static JFrame createFrame(JComponent parent, String title, JComponent component, JMenuBar menuBar) {
    JFrame frame = new JFrame(title);
    frame.setContentPane(component);
    frame.setJMenuBar(menuBar);
    return frame;
  }
  public static JInternalFrame createIFrame(JComponent parent, String title, JComponent component, JMenuBar menuBar) {
    JInternalFrame frame = new JInternalFrame(title);
    frame.setContentPane(component);
    frame.setJMenuBar(menuBar);
    return frame;
  }
}
