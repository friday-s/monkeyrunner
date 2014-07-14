/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hwh;

import com.android.monkeyrunner.MonkeyDevice;
import com.android.monkeyrunner.core.IMonkeyImage;
import com.android.monkeyrunner.core.IMonkeyDevice;
import com.android.monkeyrunner.recorder.ActionListModel;
import com.android.monkeyrunner.recorder.actions.Action;
import com.android.monkeyrunner.recorder.actions.DragAction;
import com.android.monkeyrunner.recorder.actions.DragAction.Direction;
import com.android.monkeyrunner.recorder.actions.PressAction;
import com.android.monkeyrunner.recorder.actions.TouchAction;
import com.android.monkeyrunner.recorder.actions.TypeAction;
import com.android.monkeyrunner.recorder.actions.WaitAction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * MainFrame for MonkeyRecorder.
 */
public class HwhRecorderFrame extends JFrame {
    private static final Logger LOG = Logger.getLogger(HwhRecorderFrame.class.getName());

    private final IMonkeyDevice device;

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JLabel display = null;
    private JScrollPane historyPanel = null;
    private JPanel actionPanel = null;
    private JButton waitButton = null;
    private JButton pressButton = null;
    private JButton typeButton = null;
    private JButton flingButton = null;
    private JButton exportActionButton = null;
    
    private JPanel  keyPanel = null;
    private JButton keyBackButton = null;
    private JButton keyHomeButton = null;
    private JButton keyMenuButton = null;
    
    private JPanel assistantPanel = null;
    private JButton keyInsertTextButton = null;
    private JTextField editorInsertText = null;

    private JButton refreshButton = null;

    private BufferedImage currentImage;  //  @jve:decl-index=0:
    private BufferedImage scaledImage = new BufferedImage(320, 480,
            BufferedImage.TYPE_INT_ARGB);  //  @jve:decl-index=0:

    private JList historyList;
    private ActionListModel actionListModel;
    
    private int devImageWith = 0, devImageHeight = 0;
    private int prePressedX = -1, prePressedY = -1;
    private long prePressedTime = 0;

    private final Timer refreshTimer = new Timer(200, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshDisplay();
        }
    });

    /**
     * This is the default constructor
     */
    public HwhRecorderFrame(IMonkeyDevice device) {
        this.device = device;
        initialize();
    }

    private void initialize() {
        IMonkeyImage snapshot = device.takeSnapshot();
        currentImage = snapshot.createBufferedImage();
        devImageWith = (int)(currentImage.getWidth() * 0.6);
        devImageHeight = (int)(currentImage.getHeight() * 0.6);
        scaledImage = new BufferedImage(devImageWith, devImageHeight, BufferedImage.TYPE_INT_ARGB);
        
        this.setSize(devImageWith, devImageHeight);
        this.setContentPane(getJContentPane());
        this.setTitle("HwhRecorder");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshDisplay();
            }});
        refreshTimer.start();
    }

    private void refreshDisplay() {
        IMonkeyImage snapshot = device.takeSnapshot();
        currentImage = snapshot.createBufferedImage();
        
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(currentImage, 0, 0,
        		scaledImage.getWidth(), scaledImage.getHeight(),
                null);
        g.dispose();

        display.setIcon(new ImageIcon(scaledImage));

        pack();
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            display = new JLabel();
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(display, BorderLayout.CENTER);
            jContentPane.add(getAssistantPanel(), BorderLayout.EAST);
            jContentPane.add(getActionPanel(), BorderLayout.NORTH);
            jContentPane.add(getKeyPanel(), BorderLayout.SOUTH);
            
            //display.setPreferredSize(new Dimension(320, 480));
            display.setPreferredSize(new Dimension(scaledImage.getWidth(), scaledImage.getHeight()));

            display.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    touch(event);
                }

				@Override
				public void mousePressed(MouseEvent event) {
					touch(event);
				}

				@Override
				public void mouseReleased(MouseEvent event) {
					touch(event);
				}
            });
        }
        return jContentPane;
    }
    
    private JPanel getKeyPanel() {
        if (keyPanel == null) {
        	keyPanel = new JPanel();
        	keyPanel.setLayout(new BoxLayout(getKeyPanel(), BoxLayout.X_AXIS));
        	keyPanel.add(getBackKey(), null);
        	keyPanel.add(getHomeKey(), null);
        	keyPanel.add(getMenuKey(), null);
        }
        return keyPanel;
    }
    
    private JButton getBackKey() {
        if (keyBackButton == null) {
        	keyBackButton = new JButton();
        	keyBackButton.setText("Back");
        	keyBackButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addAction(new PressAction("BACK", "downAndUp"));
                }
            });
        }
        return keyBackButton;
    }
    
    private JButton getHomeKey() {
        if (keyHomeButton == null) {
        	keyHomeButton = new JButton();
        	keyHomeButton.setText("Home");
        	keyHomeButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addAction(new PressAction("HOME", "downAndUp"));
                }
            });
        }
        return keyHomeButton;
    }
    
    private JButton getMenuKey() {
        if (keyMenuButton == null) {
        	keyMenuButton = new JButton();
        	keyMenuButton.setText("Menu");
        	keyMenuButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addAction(new PressAction("MENU", "downAndUp"));
                }
            });
        }
        return keyMenuButton;
    }
    
    private JPanel getAssistantPanel() {
        if (assistantPanel == null) {
        	assistantPanel = new JPanel();
        	assistantPanel.setLayout(new BoxLayout(getAssistantPanel(), BoxLayout.Y_AXIS));
        	assistantPanel.add(getHistoryPanel(), null);
        	assistantPanel.add(getInsertTextEditor(), null);
        	assistantPanel.add(getInsertTextKey(), null);
        }
        return assistantPanel;
    }
    
    private JButton getInsertTextKey() {
        if (keyInsertTextButton == null) {
        	keyInsertTextButton = new JButton();
        	keyInsertTextButton.setText("Insert Text");
        	keyInsertTextButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	addAction(new TypeAction(editorInsertText.getText()));
                }
            });
        }
        return keyInsertTextButton;
    }
    
    private JTextField getInsertTextEditor() {
        if (editorInsertText == null) {
        	editorInsertText = new JTextField(20);
        	editorInsertText.setToolTipText("Input here, then press \"Insert Text\".");
        }
        return editorInsertText;
    }

    /**
     * This method initializes historyPanel
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getHistoryPanel() {
        if (historyPanel == null) {
            historyPanel = new JScrollPane();
            historyPanel.getViewport().setView(getHistoryList());
        }
        return historyPanel;
    }

    private JList getHistoryList() {
        if (historyList == null) {
            actionListModel = new ActionListModel();
            historyList = new JList(actionListModel);
        }
        return historyList;
    }

    /**
     * This method initializes actionPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getActionPanel() {
        if (actionPanel == null) {
            actionPanel = new JPanel();
            actionPanel.setLayout(new BoxLayout(getActionPanel(), BoxLayout.X_AXIS));
            //actionPanel.add(getWaitButton(), null);
            //actionPanel.add(getPressButton(), null);
            //actionPanel.add(getTypeButton(), null);
            //actionPanel.add(getFlingButton(), null);
            actionPanel.add(getExportActionButton(), null);
            //actionPanel.add(getRefreshButton(), null);
        }
        return actionPanel;
    }

    /**
     * This method initializes exportActionButton
     *
     * @return javax.swing.JButton
     */
    private JButton getExportActionButton() {
        if (exportActionButton == null) {
            exportActionButton = new JButton();
            exportActionButton.setText("Export Actions");
            exportActionButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent ev) {
                    JFileChooser fc = new JFileChooser();
                    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        try {
                            actionListModel.export(fc.getSelectedFile());
                        } catch (FileNotFoundException e) {
                            LOG.log(Level.SEVERE, "Unable to save file", e);
                        }
                    }
                }
            });
        }
        return exportActionButton;
    }

    private void touch(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        // Since we scaled the image down, our x/y are scaled as well.
        double scalex = ((double) currentImage.getWidth()) / ((double) scaledImage.getWidth());
        double scaley = ((double) currentImage.getHeight()) / ((double) scaledImage.getHeight());

        x = (int) (x * scalex);
        y = (int) (y * scaley);
        
        if (x > currentImage.getWidth() || y > currentImage.getHeight()) {
        	return;
        }

        switch (event.getID()) {
            case MouseEvent.MOUSE_CLICKED:
                addAction(new TouchAction(x, y, MonkeyDevice.DOWN_AND_UP));
                break;
            case MouseEvent.MOUSE_PRESSED:
				prePressedX = x;
				prePressedY = y;
				prePressedTime = event.getWhen();
                //addAction(new TouchAction(x, y, MonkeyDevice.DOWN));
                break;
            case MouseEvent.MOUSE_RELEASED:                
                DragAction.Direction dir;
                int dx = x - prePressedX;
                int dy = y - prePressedY;
                if (Math.abs(dx) < 15 && Math.abs(dy) < 15) {
                	//addAction(new TouchAction(prePressedX, prePressedY, MonkeyDevice.DOWN));
                	//addAction(new TouchAction(x, y, MonkeyDevice.UP));
                	break;
                }
                if (Math.abs(dx) > Math.abs(dy)) {
                	dir = (dx > 0) ? DragAction.Direction.EAST: DragAction.Direction.WEST;
                } else {
                	dir = (dy > 0) ? DragAction.Direction.SOUTH : DragAction.Direction.NORTH;
                }
                addAction(new DragAction(dir, prePressedX, prePressedY, x, y, 2, 0));
                break;
        }
    }

    public void addAction(Action a) {
        actionListModel.add(a);
        try {
            a.execute(device);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to execute action!", e);
        }
    }
}
