package net.segsd.timelog.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.SwingConstants;

/**
 * Title:        Auto Grid Layout
 * Description:  Layout for creating an automatically adjusting grid layuot
 *               of components where rows and columns can vary in size, and
 *               where components can "span" multiple rows and/or collumns.
 * Copyright:    Copyright (c) 2001
 * Company:       Scott Everett Gibson Software Development
 * @author Scott Gibson
 * @version 1.0
 */

/**
 * Flexible grid-based layout manager with variable width columns and 
 * variable height rows and ability to span columns and rows.
 */
public class AutoGridLayout implements LayoutManager2 {

	/**
	 * Only a single constructor since component locations are specified
	 * when components are added.  Cell sizes are computed at layout time.
	 */
  public AutoGridLayout() {
    init();
  }

  private boolean finalized = false;
  private void init() {
    components = new Vector<Component>();
    constraints = new Hashtable<Component,Object>();
    names = new Hashtable<Component,String>();
    controls = new Hashtable<String,Object>();
    compControls = new Hashtable<Component,Hashtable<LayoutPolicy,Object>>();
    finalized = false;
    this.setHorizontalAlignment(SwingConstants.CENTER);
    this.setVerticalAlignment(SwingConstants.CENTER);
  }

  protected void finalize() {
    gridInvalid = true;

//    target = null;
    components.clear();
    constraints.clear();
    names.clear();
    compControls.clear();
    controls.clear();

    preferredSize = null;
//    minimumSize = null;
    components = null;
    constraints = null;
    names = null;
    controls = null;

    finalized = true;
  }


  public float getLayoutAlignmentX(Container target) {
    if (finalized) init();
    return 0;
  }
  public float getLayoutAlignmentY(Container target) {
    if (finalized) init();
    return 0;
  }

  // This layout manager is implemented in the following four steps:
  //  - Use the component list to build a grid of components and sizes
  //  - Use the grid and components to compute the min/pref sizes
  //    for each row/column.
  //  - Use the min/pref sizes per column along with the insets and
  //    gap policies to determine the min/pref sizes for the layout.
  //  - Use the above info along with the parent size and the alignment
  //    and stretch policies to actually place each component.
  // In order to eliminate wasted time, these steps will only be done
  // if necessary.  So, the four boolean variables below will control
  // whether one of the steps is performed.  Note that if one step
  // is performed, then it will also be necessary to perform each of
  // the steps following that step.
  private boolean gridInvalid = true;     // true if components are added/removed or if constraints are changed
  private boolean layoutInvalid = true;   // true if component sizes change
  private boolean sizesInvalid = true;    // true if insets or gaps change
  private boolean placementInvalid = true;// true if parent size changes
//  private Container target = null;

  public void invalidateLayout(Container target) {
    if (finalized) init();
    layoutInvalid = true;
//    this.target = target;
  }

  private Vector<Component> components;
  private Hashtable<Component,Object> constraints;
  private Hashtable<Component,String> names;

  public void addLayoutComponent(Component comp, Object constraints) {
    if (finalized) init();
    gridInvalid = true;
    if (!components.contains(comp)) components.addElement(comp);
    if (constraints != null) this.constraints.put(comp, constraints);
  }
  public void addLayoutComponent(String name, Component comp) {
    if (finalized) init();
    gridInvalid = true;
    if (!components.contains(comp)) components.addElement(comp);
    names.put(comp, name);
  }
  public void removeLayoutComponent(Component comp) {
    if (finalized) init();
    gridInvalid = true;
    if (names.contains(comp)) names.remove(comp);
    if (components.contains(comp)) components.remove(comp);
    components.removeElement(comp);
  }


  Dimension preferredSize;
//  Dimension minimumSize;

  public Dimension preferredLayoutSize(Container target) {
    if (finalized) init();
    setSizes(target);
    return preferredSize;
  }
  public Dimension minimumLayoutSize(Container target) {
    if (finalized) init();
    setSizes(target);
    return preferredSize;
//    return target.getMinimumSize();
  }
  public Dimension maximumLayoutSize(Container target) {
    if (finalized) init();
    return null;
//    return target.getMaximumSize();
  }
  public void layoutContainer(Container target) {
    if (finalized) init();
    setPlacement(target);
  }

  // Methods to control layout policy...
  public static enum LayoutPolicy {
    STRETCH_VERTICAL,
    STRETCH_HORIZONTAL,
    VERTICAL_ALIGNMENT,
    HORIZONTAL_ALIGNMENT,
    SCROLLBARS;
  }

  private Hashtable<Component,Hashtable<LayoutPolicy,Object>> compControls;
  private Hashtable<String,Object> controls;
  private void setControl(LayoutPolicy control, Component comp, int column, int row, Object value) {
    if (comp != null) {
      Hashtable<LayoutPolicy,Object> compControl;
      if (compControls.containsKey(comp)) {
          compControl = compControls.get(comp);
      } else {
        compControls.put(comp, compControl = new Hashtable<LayoutPolicy,Object>());
      }
      compControl.put(control, value);
    } else if (row >= 0) {
      controls.put(control+":*,"+row, value);
    } else if (column >= 0) {
      controls.put(control+":"+column+",*", value);
    } else {
      controls.put(control+":*,*", value);
    }
  }
  private Object getControl(LayoutPolicy control, Component comp, int column, int row) {
    if (comp != null) {
      Hashtable<LayoutPolicy,Object> compControl;
      if (compControls.containsKey(comp)) {
        compControl = compControls.get(comp);
        if (compControl.containsKey(control))
          return compControl.get(control);
      }
    }
    if (row >= 0) {
      if (controls.containsKey(control+":*,"+row))
        return controls.get(control+":*,"+row);
    }
    if (column >= 0) {
      if (controls.containsKey(control+":"+column+",*"))
        return controls.get(control+":"+column+",*");
    }
    if (controls.containsKey(control+":*,*"))
      return controls.get(control+":*,*");
    return null;
  }
  private void setBooleanControl(LayoutPolicy control, Component comp, int column, int row, boolean value) {
    Boolean booleanValue = value ? Boolean.TRUE : Boolean.FALSE;
    setControl(control, comp, column, row, booleanValue);
  }
  private boolean getBooleanControl(LayoutPolicy control, Component comp, int column, int row) {
    Boolean value = (Boolean) getControl(control, comp, column, row);
    return value == null ? false : value.booleanValue();
  }
  private void setIntegerControl(LayoutPolicy control, Component comp, int column, int row, int value) {
    setControl(control, comp, column, row, new Integer(value));
  }
  private int getIntegerControl(LayoutPolicy control, Component comp, int column, int row) {
    Integer value = (Integer) getControl(control, comp, column, row);
    return value == null ? 0 : value.intValue();
  }

  private void setStretchableHorizontal(Component comp, int column, int row, boolean stretchable) {
    layoutInvalid = true;
    setBooleanControl(LayoutPolicy.STRETCH_HORIZONTAL, comp, column, row, stretchable);
  }
  private void setStretchableVertical(Component comp, int column, int row, boolean stretchable) {
    layoutInvalid = true;
    setBooleanControl(LayoutPolicy.STRETCH_VERTICAL, comp, column, row, stretchable);
  }
  private boolean getStretchableHorizontal(Component comp, int column, int row) {
    return getBooleanControl(LayoutPolicy.STRETCH_HORIZONTAL, comp, column, row);
  }
  private boolean getStretchableVertical(Component comp, int column, int row) {
    return getBooleanControl(LayoutPolicy.STRETCH_VERTICAL, comp, column, row);
  }

  // Stretchable in both vertical and horizontal directions...
  public void setStretchableHorizontal(boolean stretchable) {
    if (finalized) init();
    setStretchableHorizontal(null, -1, -1, stretchable);
  }
  public void setStretchableVertical(boolean stretchable) {
    if (finalized) init();
    setStretchableVertical(null, -1, -1, stretchable);
  }
  public void setStretchableRow(int row, boolean stretchable) {
    if (finalized) init();
    setStretchableVertical(null, -1, row, stretchable);
  }
  public void setStretchableColumn(int column, boolean stretchable) {
    if (finalized) init();
    setStretchableHorizontal(null, column, -1, stretchable);
  }
  public void setStretchableComponent(Component comp, boolean stretchableVertical, boolean stretchableHorizontal) {
    if (finalized) init();
    setStretchableHorizontal(comp, -1, -1, stretchableHorizontal);
    setStretchableVertical(comp, -1, -1, stretchableVertical);
  }

  // Automatically create scrollbars...
  private void setScrollbars(Component comp, int column, int row, boolean enable) {
    sizesInvalid = true;
    setBooleanControl(LayoutPolicy.SCROLLBARS, comp, column, row, enable);
  }
  private boolean getScrollbars(Component comp, int column, int row) {
    return getBooleanControl(LayoutPolicy.SCROLLBARS, comp, column, row);
  }

  public void setScrollbars(Component comp, boolean enable) {
    if (finalized) init();
    setScrollbars(comp, -1, -1, enable);
  }
  public void setScrollbarsRow(int row, boolean enable) {
    if (finalized) init();
    setScrollbars(null, -1, row, enable);
  }
  public void setScrollbarsColumn(int column, boolean enable) {
    if (finalized) init();
    setScrollbars(null, column, -1, enable);
  }

  // Component Alignment...
  private void setHorizontalAlignment(Component comp, int column, int row, int horizontalAlignment) {
    placementInvalid = true;
    setIntegerControl(LayoutPolicy.HORIZONTAL_ALIGNMENT, comp, column, row, horizontalAlignment);
  }
  private int getHorizontalAlignment(Component comp, int column, int row) {
    return getIntegerControl(LayoutPolicy.HORIZONTAL_ALIGNMENT, comp, column, row);
  }
  private void setVerticalAlignment(Component comp, int column, int row, int verticalAlignment) {
    placementInvalid = true;
    setIntegerControl(LayoutPolicy.VERTICAL_ALIGNMENT, comp, column, row, verticalAlignment);
  }
  private int getVerticalAlignment(Component comp, int column, int row) {
    return getIntegerControl(LayoutPolicy.VERTICAL_ALIGNMENT, comp, column, row);
  }

  public void setHorizontalAlignment(int horizontalAlignment) {
    setHorizontalAlignment(null, -1, -1, horizontalAlignment);
  }
  public void setVerticalAlignment(int verticalAlignment) {
    setVerticalAlignment(null, -1, -1, verticalAlignment);
  }
  public void setHorizontalAlignmentRow(int row, int horizontalAlignment) {
    setHorizontalAlignment(null, -1, row, horizontalAlignment);
  }
  public void setHorizontalAlignmentColumn(int column, int horizontalAlignment) {
    setHorizontalAlignment(null, column, -1, horizontalAlignment);
  }
  public void setVerticalAlignmentRow(int row, int verticalAlignment) {
    setVerticalAlignment(null, -1, row, verticalAlignment);
  }
  public void setVerticalAlignmentColumn(int column, int verticalAlignment) {
    setVerticalAlignment(null, column, -1, verticalAlignment);
  }
  public void setHorizontalAlignment(Component comp, int horizontalAlignment) {
    setHorizontalAlignment(comp, -1, -1, horizontalAlignment);
  }
  public void setVerticalAlignment(Component comp, int verticalAlignment) {
    setVerticalAlignment(comp, -1, -1, verticalAlignment);
  }

  // Insets...
  private int horizontalInset = 5;
  private int verticalInset = 5;
  public void setInset(int inset) {
    this.setHorizontalInset(inset);
    this.setVerticalInset(inset);
  }
  public void setHorizontalInset(int horizontalInset) {
    sizesInvalid = true;
    this.horizontalInset = horizontalInset;
  }
  public void setVerticalInset(int verticalInset) {
    sizesInvalid = true;
    this.verticalInset = verticalInset;
  }

  // Gaps...
  private int horizontalGap = 5;
  private int verticalGap = 5;
  public void setGap(int gap) {
    this.setHorizontalGap(gap);
    this.setVerticalGap(gap);
  }
  public void setHorizontalGap(int horizontalGap) {
    sizesInvalid = true;
    this.horizontalGap = horizontalGap;
  }
  public void setVerticalGap(int verticalGap) {
    sizesInvalid = true;
    this.verticalGap = verticalGap;
  }

  public void setGapAndInset(int gap, int inset) {
    this.setGap(gap);
    this.setInset(inset);
  }

  private boolean distributeExtraWidth = true;
  private boolean distributeExtraHeight = true;
  public void setDistributeExtraWidth(boolean value) {
    if (value != distributeExtraWidth) {
      sizesInvalid = true;
      distributeExtraWidth = value;
      if (value) setCenterHorizontal(false);
    }
  }
  public void setDistributeExtraHeight(boolean value) {
    if (value != distributeExtraHeight) {
      sizesInvalid = true;
      distributeExtraHeight = value;
      if (value) setCenterVertical(false);
    }
  }

  private boolean centerVertical = false;
  private boolean centerHorizontal = false;
  public void setCenterVertical(boolean value) {
    if (value != centerVertical) {
      sizesInvalid = true;
      centerVertical = value;
      if (value) setDistributeExtraHeight(false);
    }
  }
  public void setCenterHorizontal(boolean value) {
    if (value != centerHorizontal) {
      sizesInvalid = true;
      centerHorizontal = value;
      if (value) setDistributeExtraWidth(false);
    }
  }

  private int prefColumns = 1;
  private int prefRows = 1;
  private int columns;
  private int rows;

  public int getPreferredColumns() {
    return prefColumns;
  }
  public void setPreferredColumns(int prefColumns) {
    this.prefColumns= prefColumns;
  }
  public int getPreferredRows() {
    return prefRows;
  }
  public void setPreferredRows(int prefRows) {
    this.prefRows = prefRows;
  }

  private int[] columnPref;
//  private int[] columnMin;
//  private int[] columnSize; // Actuals calculated during setPlacement
  private int[] rowPref;
//  private int[] rowMin;
//  private int[] rowSize; // Actuals calculated during setPlacement
  private Component[][] compGrid;

  // Primary work methods...
  private void setGrid(Container target) {
    if (!gridInvalid) return;

    // Calculate number of columns/rows...
    columns = prefColumns;
    rows = prefRows;
    int x;
    int y;
    int nx;
    int ny;
    Component[] comps = target.getComponents();
    int[] compConstraints;
    int looseComponents = 0;
    for (int i = 0; i < comps.length; i++) {
      if (constraints.containsKey(comps[i])) {
        compConstraints = (int[]) constraints.get(comps[i]);
        x = compConstraints[0];
        nx = compConstraints.length > 2 ? compConstraints[2] : 1;
        y = compConstraints[1];
        ny = compConstraints.length > 3 ? compConstraints[3] : 1;
        columns = Math.max(columns, x + nx);
        rows = Math.max(rows, y + ny);
      } else {
        // Count "loose" components...
        looseComponents++;
      }
    }

    // Now, add space for the looseComponents...
    if (looseComponents > 0) rows += 1 + (int) (looseComponents / columns);

    // Allocate column/row size arrays...
    columnPref = new int[columns];
//    columnMin = new int[columns];
//    columnSize = new int[columns];
    rowPref = new int[rows];
//    rowMin = new int[rows];
//    rowSize = new int[rows];

    compGrid = new Component[columns][rows];

    for (int i = 0; i < comps.length; i++) {
      if (constraints.containsKey(comps[i])) {
        compConstraints = (int[]) constraints.get(comps[i]);
        x = compConstraints[0];
        nx = compConstraints.length > 2 ? compConstraints[2] : 1;
        y = compConstraints[1];
        ny = compConstraints.length > 3 ? compConstraints[3] : 1;
        for (int xSpan = 0; xSpan < nx; xSpan++)
          for (int ySpan = 0; ySpan < ny; ySpan++)
            compGrid[x + xSpan][y + ySpan] = comps[i];
      }
    }

    x = 0;
    y = 0;

    // Now, place the "looseComponents"...
    for (int i = 0; i < comps.length; i++) {
      if (!constraints.containsKey(comps[i])) {
        while (compGrid[x][y] != null && y < rows) {
          if (++x == columns) { x = 0; y++; }
        }
        if (y < rows) {
          constraints.put(comps[i], new int[] { x, y });
          compGrid[x][y] = comps[i];
        } else {
          throw new RuntimeException("Ran out of room in component grid while placing loose components.");
        }
      }
    }

    gridInvalid = false;
    layoutInvalid = true;
  }

  private void setLayout(Container target) {
    setGrid(target);
    if (!layoutInvalid) return;
//    System.err.println("setting layout...");

    Dimension compPref;
    Dimension compMin;
    int wPref;
    int hPref;
//    Dimension compMin;
    Component comp;
    int[] compConstraints;
    // How much space to allocate for scrollbars...
    int scrollbarWidth = 15;
    int scrollbarHeight = 15;
    int cSpan;
    int rSpan;

    for (int c = 0; c < columns; c++) {
      for (int r = 0; r < rows; r++) {
        if (c == 0) rowPref[r] = -1;
        if (r == 0) columnPref[c] = -1;
        comp = compGrid[c][r];
        if (comp == null) {
//          System.err.println("Unexpected empty compGrid["+c+"]["+r+"]...\nLaying out: "+target);
          continue;
        }
        compConstraints = (int[]) constraints.get(comp);
//        if (compConstraints[0] != c || compConstraints[1] != r) continue;
        if (compConstraints.length > 2) {
          cSpan = compConstraints[2];
          rSpan = compConstraints[3];
        } else {
          cSpan = rSpan = 1;
        }
        if (compConstraints.length > 4) {
          wPref = compConstraints[4];
          hPref = compConstraints[5];
        } else wPref = hPref = 0;

        compPref = comp.getPreferredSize();
        compMin = comp.getMinimumSize();
        if (wPref == 0)
          wPref = Math.max(Math.max(compPref.width,compMin.width),10);
        if (hPref == 0)
          hPref = Math.max(Math.max(compPref.height,compMin.height),10);
        if (getScrollbars(comp, c, r)) {
          wPref += scrollbarWidth;
//          compMin.width += scrollbarWidth;
          hPref += scrollbarHeight;
//          compMin.height += scrollbarHeight;
        }
        if (rSpan == 1) {
          if (rowPref[r] == -1) {
            rowPref[r] = hPref;
//            rowMin[r] = compMin.height;
          } else {
            rowPref[r] = Math.max(rowPref[r], hPref);
//            rowMin[r] = Math.max(rowMin[r], compMin.height);
          }
        }
        if (cSpan == 1) {
          if (columnPref[c] == -1) {
            columnPref[c] = wPref;
//            columnMin[c] = compMin.width;
          } else {
            columnPref[c] = Math.max(columnPref[c], wPref);
//            columnMin[c] = Math.max(columnMin[c], compMin.width);
          }
        }
      }
    }

    // Loop through checking whether any spanned components require more
    // space -- if so, then increase the size of all spanned columns...
    for (int c = 0; c < columns; c++) {
      for (int r = 0; r < rows; r++) {
        comp = compGrid[c][r];
        if (comp == null) {
//          System.err.println("Unexpected empty compGrid["+c+"]["+r+"]...\nLaying out: "+target);
          continue;
        }
        compConstraints = (int[]) constraints.get(comp);
//        if (compConstraints[0] != c || compConstraints[1] != r) continue;
        if (compConstraints.length > 2) {
          cSpan = compConstraints[2];
          rSpan = compConstraints[3];
        } else {
          cSpan = rSpan = 1;
        }
        if (compConstraints.length > 4) {
          wPref = compConstraints[4];
          hPref = compConstraints[5];
        } else
          wPref = hPref = 0;

        compPref = comp.getPreferredSize();
        compMin = comp.getMinimumSize();
        if (wPref == 0)
          wPref = Math.max(Math.max(compPref.width,compMin.width),10);
        if (hPref == 0)
          hPref = Math.max(Math.max(compPref.height,compMin.height),10);
//        compMin = comp.getMinimumSize();
        if (getScrollbars(comp, c, r)) {
          wPref += scrollbarWidth;
//          compMin.width += scrollbarWidth;
          hPref += scrollbarHeight;
//          compMin.height += scrollbarHeight;
        }

        if (rSpan > 1 && r == compConstraints[1]) {
          int actualH = 0;
          for (int spanR = 0; spanR < rSpan; spanR++)
            actualH += rowPref[r+spanR];
          if (actualH < hPref) {
            int additionalH = (int) (((rSpan - 1) + hPref - actualH) / rSpan);
            for (int spanR = 0; spanR < rSpan; spanR++)
              rowPref[r + spanR] += additionalH;
          }
        }
        if (cSpan > 1 && c == compConstraints[0]) {
          int actualW = 0;
          for (int spanC = 0; spanC < cSpan; spanC++)
            actualW += columnPref[c+spanC];
          if (actualW < wPref) {
            int additionalW = (int) (((cSpan - 1) + wPref - actualW) / cSpan);
            for (int spanC = 0; spanC < cSpan; spanC++)
              columnPref[c + spanC] += additionalW;
          }
        }
      }
    }

    // Now see whether the comps spanning rows or columns require any adjustments...

    layoutInvalid = false;
    sizesInvalid = true;
  }

  private void setSizes(Container target) {
    setLayout(target);
    if (!sizesInvalid) return;
//    System.err.println("setting sizes...");

    int wPref = horizontalInset * 2 + (columns - 1) * horizontalGap;
    int hPref = verticalInset * 2 + (rows - 1) * verticalGap;
//    int wMin = wPref;
//    int hMin = hPref;
    for (int c = 0; c < columns; c++) {
      if (columnPref[c] > 0) wPref += columnPref[c];
//      if (columnMin[c] > 0) wMin += columnMin[c];
    }
    for (int r = 0; r < rows; r++) {
      if (rowPref[r] > 0) hPref += rowPref[r];
//      if (rowMin[r] > 0) hMin += rowMin[r];
    }
    Dimension targetMax = target.getMaximumSize();
    Dimension targetMin = target.getMaximumSize();
    if (targetMax != null && targetMax.width < wPref) wPref = targetMax.width;
    if (targetMax != null && targetMax.height < hPref) hPref = targetMax.height;
    if (targetMin != null && targetMin.width > wPref) wPref = targetMin.width;
    if (targetMin != null && targetMin.height > hPref) hPref = targetMin.height;
    preferredSize = new Dimension(wPref, hPref);
//    minimumSize = new Dimension(wMin, hMin);

    sizesInvalid = false;
    placementInvalid = true;
  }

  private void setPlacement(Container target) {
    setSizes(target);
    if (!placementInvalid) return;

    // First, find out what size we're working with...
    Dimension actualSize = target.getSize();

    Dimension baseSize = new Dimension(preferredSize.width, preferredSize.height);
    int extraWidth = actualSize.width - baseSize.width;
    int extraHeight = actualSize.height - baseSize.height;
//    System.err.println("Base size = "+baseSize);
//    System.err.println("Preferred size = "+preferredSize);
//    System.err.println("Minimum size = "+minimumSize);
//    System.err.println("Actual size = "+actualSize);

    int[] columnWidth = new int[columns];
    int[] rowHeight = new int[rows];

    int[] extraColWidth = new int[columns];
    int[] extraRowHeight = new int[rows];

    boolean[] stretchColumns = new boolean[columns];
    boolean[] stretchRows    = new boolean[rows];
    int stretchColumnCount = 0;
    int stretchRowCount = 0;
    int extraHorizontalInset = centerHorizontal ? extraWidth / 2 : 0;
    int extraVerticalInset = centerVertical ? extraHeight / 2 : 0;

    for (int c = 0; c < columns; c++) {
      if (stretchColumns[c] = getStretchableHorizontal(null, c, -1))
        stretchColumnCount++;
    }
    for (int r = 0; r < rows; r++) {
      if (stretchRows[r] = getStretchableVertical(null, -1, r))
        stretchRowCount++;
    }

    int stretchWidth = 0;
    int shrinkWidth = 0;
    int extraWidthPerColumn = 0;
    if (stretchColumnCount > 0) stretchWidth = extraWidth / stretchColumnCount - 1;
    else if (extraWidth < 0) {
      shrinkWidth = extraWidth;
    } else if (distributeExtraWidth) {
      extraWidthPerColumn = extraWidth / columns - 1;
    }
    int stretchHeight = 0;
    int shrinkHeight = 0;
    int extraHeightPerRow = 0;
    if (stretchRowCount > 0) stretchHeight = extraHeight / stretchRowCount - 1;
    else if (extraHeight < 0) {
      shrinkHeight = extraHeight;
    } else if (distributeExtraHeight) {
      extraHeightPerRow = extraHeight / rows - 1;
    }

//System.err.println("stretchHeight: "+stretchHeight+"   stretchWidth: "+stretchWidth);
//System.err.println("extraHeightPerRow: "+extraHeightPerRow+"   extraWidthPerColumn: "+extraWidthPerColumn);

    for (int c = 0; c < columns; c++) {
      columnWidth[c] = columnPref[c] + (int) (shrinkWidth * ((double) (columnPref[c] + 1) / preferredSize.width));
    }
    for (int r = 0; r < rows; r++) {
      rowHeight[r] = rowPref[r] + (int) (shrinkHeight * ((double) (rowPref[r] + 1) / preferredSize.height));
    }

    for (int c = 0; c < columns; c++) {
      if (stretchColumns[c]) columnWidth[c] += stretchWidth;
      extraColWidth[c] = extraWidthPerColumn;
    }
    for (int r = 0; r < rows; r++) {
      if (stretchRows[r]) rowHeight[r] += stretchHeight;
      extraRowHeight[r] = extraHeightPerRow;
    }

    // Now, use the policies to actually place each component...
    int[] compConstraints;
    int c;
    int r;
    int nc;
    int nr;
    int x;
    int y;
    int w;
    int h;
    int vAlign;
    int hAlign;
    int ew;
    int eh;
    Component[] comps = target.getComponents();
    for (int i = 0; i < comps.length; i++) {
      compConstraints = (int[]) constraints.get(comps[i]);
      c = compConstraints[0];
      r = compConstraints[1];
      nc = compConstraints.length > 2 ? compConstraints[2] : 1;
      nr = compConstraints.length > 3 ? compConstraints[3] : 1;
      x = horizontalInset + (horizontalGap * c) + extraHorizontalInset;
      y = verticalInset + (verticalGap * r) + extraVerticalInset;
      w = (horizontalGap * (nc - 1));
      h = (verticalGap * (nr - 1));
      vAlign = this.getVerticalAlignment(comps[i],c,r);
      hAlign = this.getHorizontalAlignment(comps[i],c,r);
      ew = eh = 0;
      for (int cSpan = 0; cSpan < nc; cSpan++) {
        w += columnWidth[c + cSpan];
        ew += extraColWidth[c + cSpan];
      }
      for (int rSpan = 0; rSpan < nr; rSpan++) {
        h += rowHeight[r + rSpan];
        eh += extraRowHeight[r + rSpan];
      }
      if (getStretchableHorizontal(comps[i], c, r)) {
        w += ew;
        ew = 0;
      }
      if (getStretchableVertical(comps[i], c, r)) {
        h += eh;
        eh = 0;
      }
      if (vAlign == SwingConstants.CENTER) y += eh / 2;
      if (vAlign == SwingConstants.BOTTOM) y += eh;
      if (hAlign == SwingConstants.CENTER) x += ew / 2;
      if (hAlign == SwingConstants.RIGHT) x += ew;
      for (c = 0; c < compConstraints[0]; c++) x += columnWidth[c] + extraColWidth[c];
      for (r = 0; r < compConstraints[1]; r++) y += rowHeight[r] + extraRowHeight[r];
      if (w == 0 || h == 0) {
        System.err.println(" 0 size: Comp = "+comps[i]);
        System.err.println("Setting bounds: "+x+","+y+","+w+","+h);
      }
      if (x + w > actualSize.width || y + h > actualSize.height) {
        System.err.println("off viewable area: Comp = "+comps[i]);
        System.err.println("Setting bounds: "+x+","+y+","+w+","+h);
      }
      comps[i].setBounds(x, y, w, h);
    }

    placementInvalid = false;
  }

  // Methods to assist in assigning row/column locations...
  private int next_r = 0;
  private int next_c = 0;
  private boolean[][] isOccuppied = new boolean[10][10];
  private boolean[][] isRoot = new boolean[10][10];

  private void growOccuppied() {
    boolean[][] save1 = isOccuppied;
    boolean[][] save2 = isRoot;
    isOccuppied  = new boolean[save1.length + 10][save1[0].length + 10];
    isRoot  = new boolean[save2.length + 10][save2[0].length + 10];
    for (int i = 0; i < save1.length + 10; i++) {
      for (int j = 0; j < save1[0].length + 10; j++) {
        if (i < save1.length && j < save1[0].length) {
          isOccuppied[i][j] = save1[i][j];
          isRoot[i][j] = save2[i][j];
        } else {
          isRoot[i][j] = isOccuppied[i][j] = false;
        }
      }
    }
  }

  private void markOccuppied(int c, int r, int nc, int nr) {
//    int newC = 0;
//    int newR = 0;
//    if (isOccuppied.length <= c + nc) newC = c + nc + 10;
//    if (isOccuppied[0].length <= r + nr) newR = r + nr + 10;
    if (isOccuppied.length <= c + nc || isOccuppied[0].length <= r + nr) {
      growOccuppied();
    }
    for (int cSpan = 0; cSpan < nc; cSpan++) {
      for (int rSpan = 0; rSpan < nr; rSpan++) {
        isOccuppied[c + cSpan][r + rSpan] = true;
        isRoot[c + cSpan][r + rSpan] = (rSpan == 0 && cSpan == 0);
      }
    }
  }

  public int[] first() {
    next_c = next_r = 0;
    int[] constr = new int[] { next_c, next_r, 1, 1 };
    markOccuppied(next_c, next_r, 1, 1);
    return constr;
  }
  public int[] first(int nc, int nr) {
    next_c = next_r = 0;
    int[] constr = new int[] { next_c, next_r, nc, nr };
    markOccuppied(next_c, next_r, nc, nr);
    return constr;
  }
  public int[] first(int nc, int nr, int w, int h) {
    next_c = next_r = 0;
    int[] constr = new int[] { next_c, next_r, nc, nr, w, h };
    markOccuppied(next_c, next_r, nc, nr);
    return constr;
  }

  public int[] nextRow() {
    ++next_r; next_c = 0;
    while (isOccuppied[next_c][next_r]) {
      if (++next_c == isOccuppied.length)
        growOccuppied();
    }
    int[] constr = new int[] { next_c, next_r, 1, 1 };
    markOccuppied(next_c, next_r, 1, 1);
    return constr;
  }
  public int[] nextRow(int nc, int nr) {
    ++next_r; next_c = 0;
    while (isOccuppied[next_c][next_r]) {
      if (++next_c == isOccuppied.length)
        growOccuppied();
    }
    int[] constr = new int[] { next_c, next_r, nc, nr };
    markOccuppied(next_c, next_r, nc, nr);
    return constr;
  }
  public int[] nextRow(int nc, int nr, int w, int h) {
    ++next_r; next_c = 0;
    while (isOccuppied[next_c][next_r]) {
      if (++next_c == isOccuppied.length)
        growOccuppied();
    }
    int[] constr = new int[] { next_c, next_r, nc, nr, w, h };
    markOccuppied(next_c, next_r, nc, nr);
    return constr;
  }

  public int[] nextCol() {
    ++next_c;
    while (isOccuppied[next_c][next_r]) {
      if (isOccuppied.length == ++next_c)
        growOccuppied();
    }
    int[] constr = new int[] { next_c, next_r, 1, 1 };
    markOccuppied(next_c, next_r, 1, 1);
    return constr;
  }
  public int[] nextCol(int nc, int nr) {
    ++next_c;
    while (isOccuppied[next_c][next_r]) {
      if (isOccuppied.length == ++next_c)
        growOccuppied();
    }
    int[] constr = new int[] { next_c, next_r, nc, nr };
    markOccuppied(next_c, next_r, nc, nr);
    return constr;
  }
  public int[] nextCol(int nc, int nr, int w, int h) {
    ++next_c;
    while (isOccuppied[next_c][next_r]) {
      if (isOccuppied.length == ++next_c)
        growOccuppied();
    }
    int[] constr = new int[] { next_c, next_r, nc, nr, w, h };
    markOccuppied(next_c, next_r, nc, nr);
    return constr;
  }

}
