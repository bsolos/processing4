/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2013-22 The Processing Foundation

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import processing.app.Base;
import processing.app.Language;
import processing.app.Platform;
import processing.app.SketchReference;


public class SketchbookFrame extends JFrame {
  protected Base base;


  public SketchbookFrame(final Base base) {
    super(Language.text("sketchbook"));
    this.base = base;

    final ActionListener listener = e -> setVisible(false);
    Toolkit.registerWindowCloseKeys(getRootPane(), listener);
    Toolkit.setIcon(this);

    Container pane = getContentPane();
    pane.setLayout(new BorderLayout());

    updateCenterPanel();

    Container buttons = Box.createHorizontalBox();

    JButton addButton = new JButton("Show Folder");
    addButton.addActionListener(e -> Platform.openFolder(Base.getSketchbookFolder()));
    buttons.add(Box.createHorizontalGlue());
    buttons.add(addButton, BorderLayout.WEST);

    JButton refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(e -> base.rebuildSketchbook());
    buttons.add(refreshButton, BorderLayout.EAST);
    buttons.add(Box.createHorizontalGlue());

    final int high = addButton.getPreferredSize().height;
    final int wide = 4 * Toolkit.getButtonWidth() / 3;
    addButton.setPreferredSize(new Dimension(wide, high));
    refreshButton.setPreferredSize(new Dimension(wide, high));

    JPanel buttonPanel = new JPanel();  // adds extra border
    // wasn't necessary to set a border b/c JPanel adds plenty
    //buttonPanel.setBorder(new EmptyBorder(3, 0, 3, 0));
    buttonPanel.add(buttons);
    pane.add(buttonPanel, BorderLayout.SOUTH);

    pack();
  }


  private JTree rebuildTree() {
    final JTree tree = new JTree(base.buildSketchbookTree());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles(true);
    tree.expandRow(0);
    tree.setRootVisible(false);

    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

          int selRow = tree.getRowForLocation(e.getX(), e.getY());
          //TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
          //if (node != null && node.isLeaf() && node.getPath().equals(selPath)) {
          if (node != null && node.isLeaf() && selRow != -1) {
            SketchReference sketch = (SketchReference) node.getUserObject();
            base.handleOpen(sketch.getPath());
          }
        }
      }
    });

    tree.addKeyListener(new KeyAdapter() {
      // ESC doesn't fire keyTyped(), so we have to catch it on keyPressed
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          setVisible(false);
        }
      }

      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
          DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
          if (node != null && node.isLeaf()) {
            SketchReference sketch = (SketchReference) node.getUserObject();
            base.handleOpen(sketch.getPath());
          }
        }
      }
    });

    final int border = Toolkit.zoom(5);
    tree.setBorder(new EmptyBorder(border, border, border, border));
    if (Platform.isMacOS()) {
      tree.setToggleClickCount(2);
    } else {
      tree.setToggleClickCount(1);
    }

    // Special cell renderer that takes the UI zoom into account
    tree.setCellRenderer(new ZoomTreeCellRenderer());

    return tree;
  }


  private void updateCenterPanel() {
    JTree tree = rebuildTree();
    Container panel;

    // Check whether sketchbook is empty or not
    TreeModel treeModel = tree.getModel();
    if (treeModel.getChildCount(treeModel.getRoot()) != 0) {
      JScrollPane treePane = new JScrollPane(tree);
      treePane.setPreferredSize(Toolkit.zoom(250, 450));
      treePane.setBorder(new EmptyBorder(0, 0, 0, 0));

      //getContentPane().add(treePane);
      panel = treePane;

    } else {
      JPanel emptyPanel = new JPanel();
      emptyPanel.setBackground(Color.WHITE);
      emptyPanel.setPreferredSize(Toolkit.zoom(250,450));

      JLabel emptyLabel = new JLabel("Empty Sketchbook");
      emptyLabel.setForeground(Color.GRAY);
      emptyPanel.add(emptyLabel);

      //setContentPane(emptyPanel);
      panel = emptyPanel;
    }

    Container pane = getContentPane();
    //pane.setLayout(new BorderLayout());
    BorderLayout layout = (BorderLayout) pane.getLayout();
    Component comp = layout.getLayoutComponent(BorderLayout.CENTER);
    if (comp != null) {
      pane.remove(comp);
    }
    pane.add(panel, BorderLayout.CENTER);
  }


  public void rebuild() {
    updateCenterPanel();
    // After replacing the tree/panel, this calls the layout manager,
    // otherwise it'll just leave a frozen-looking component until
    // the window is closed and re-opened.
    getContentPane().validate();
  }


  public void setVisible() {
    // TODO The ExamplesFrame code doesn't do this, is it necessary?
    //      Either one of them is wrong, or this is hiding a bug [fry 150811]
    EventQueue.invokeLater(() -> {
      // Space for the editor plus a li'l gap
      int roughWidth = getWidth() + 20;
      // If no window open, or the editor is at the edge of the screen
      Editor editor = base.getActiveEditor();
      Point p = editor.getLocation();
      if (editor == null || p.x < roughWidth) {
        // Center the window on the screen
        setLocationRelativeTo(null);
      } else {
        // Open the window relative to the editor
        setLocation(p.x - roughWidth, p.y);
      }
      setVisible(true);
    });
  }
}
