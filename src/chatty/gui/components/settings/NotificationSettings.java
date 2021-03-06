
package chatty.gui.components.settings;

import chatty.Chatty;
import chatty.gui.GuiUtil;
import static chatty.gui.GuiUtil.SMALL_BUTTON_INSETS;
import chatty.gui.components.LinkLabel;
import chatty.gui.notifications.Notification;
import chatty.util.Sound;
import chatty.util.settings.Settings;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author tduva
 */
public class NotificationSettings extends SettingsPanel {
    
    public final static long NOTIFICATION_TYPE_CUSTOM = 0;
    public final static long NOTIFICATION_TYPE_TRAY = 1;
    
    private final LinkLabel userReadPermission;
    private final JCheckBox requestFollowedStreams;
    
    private final ComboLongSetting nType;
    private final ComboLongSetting nScreen;
    private final ComboLongSetting nPosition;
    private final DurationSetting nDisplayTime;
    private final DurationSetting nMaxDisplayTime;
    private final JCheckBox userActivity;
    
    private final PathSetting soundsPath;
    
    private final JLabel filesResult = new JLabel();
    
    private final NotificationEditor editor;
    
    public NotificationSettings(SettingsDialog d, Settings settings) {
        editor = new NotificationEditor(d, settings);
        
        GridBagConstraints gbc;

        //=======================
        // Notification Settings
        //=======================
        JPanel notificationSettings = new JPanel(new GridBagLayout());

        gbc = d.makeGbc(0, 0, 4, 1, GridBagConstraints.WEST);
        gbc.insets = new Insets(10,5,4,5);
        
        Map<Long, String> nTypeOptions = new LinkedHashMap<>();
        nTypeOptions.put(NOTIFICATION_TYPE_CUSTOM, "Chatty Notifications");
        nTypeOptions.put(NOTIFICATION_TYPE_TRAY, "Tray Notifications (OS dependant)");
        nType = new ComboLongSetting(nTypeOptions);

        nType.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                updateSettingsState();
            }
        });
        d.addLongSetting("nType", nType);
        notificationSettings.add(nType, gbc);
        
        notificationSettings.add(new JLabel("Position:"),
                d.makeGbc(0, 1, 1, 1, GridBagConstraints.EAST));
        
        Map<Long, String> nPositionOptions = new LinkedHashMap<>();
        nPositionOptions.put(Long.valueOf(0), "Top-Left");
        nPositionOptions.put(Long.valueOf(1), "Top-Right");
        nPositionOptions.put(Long.valueOf(2), "Bottom-Left");
        nPositionOptions.put(Long.valueOf(3), "Bottom-Right");
        nPosition = new ComboLongSetting(nPositionOptions);
        d.addLongSetting("nPosition", nPosition);
        gbc = d.makeGbc(1, 1, 1, 1);
        notificationSettings.add(nPosition, gbc);
        
        notificationSettings.add(new JLabel("Screen:"),
                d.makeGbc(0, 2, 1, 1, GridBagConstraints.EAST));
        
        Map<Long, String> nScreenOptions = new LinkedHashMap<>();
        nScreenOptions.put(Long.valueOf(-1), "Auto");
        nScreenOptions.put(Long.valueOf(0), "1");
        nScreenOptions.put(Long.valueOf(1), "2");
        nScreenOptions.put(Long.valueOf(2), "3");
        nScreenOptions.put(Long.valueOf(3), "4");
        nScreen = new ComboLongSetting(nScreenOptions);
        d.addLongSetting("nScreen", nScreen);
        notificationSettings.add(nScreen,
                d.makeGbc(1, 2, 1, 1, GridBagConstraints.WEST));

        
        notificationSettings.add(new JLabel("Display Time:"),
                d.makeGbc(2, 1, 1, 1, GridBagConstraints.EAST));
        
        nDisplayTime = new DurationSetting(3, true);
        d.addLongSetting("nDisplayTime", nDisplayTime);
        notificationSettings.add(nDisplayTime,
                d.makeGbc(3, 1, 1, 1, GridBagConstraints.WEST));
        
        
        userActivity = d.addSimpleBooleanSetting("nActivity", "No User Activity:",
                "Display longer unless the mouse was recently moved");
        notificationSettings.add(userActivity,
                d.makeGbc(2, 2, 1, 1, GridBagConstraints.EAST));
        //main.add(new JLabel("Max Display Time:"), d.makeGbc(2, 2, 1, 1, GridBagConstraints.EAST));
        
        nMaxDisplayTime = new DurationSetting(3, true);
        d.addLongSetting("nMaxDisplayTime", nMaxDisplayTime);
        notificationSettings.add(nMaxDisplayTime,
                d.makeGbc(3, 2, 1, 1, GridBagConstraints.WEST));
        
        //================
        // Sound Settings
        //================
        JPanel soundSettings = new JPanel(new GridBagLayout());
        
        gbc = d.makeGbc(0, 0, 3, 1, GridBagConstraints.WEST);
        JCheckBox soundsEnabled = d.addSimpleBooleanSetting("sounds", "Enable sounds (uncheck to mute)",
                "Use this to enable/disable all sounds.");
        soundSettings.add(soundsEnabled, gbc);
        
        gbc = d.makeGbc(0, 1, 3, 1, GridBagConstraints.WEST);
        gbc.insets = new Insets(8,10,2,6);
        soundSettings.add(new JLabel("Chatty looks for sound files (.wav) in this "
                + "folder:"), gbc);
        
        gbc = d.makeGbc(0, 2, 3, 1);
        gbc.insets = new Insets(3,10,3,8);
        
        PathSetting path = new PathSetting(d, Chatty.getSoundDirectory());
        d.addStringSetting("soundsPath", path);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        soundSettings.add(path, gbc);
        soundsPath = path;
        path.setPathChangeListener(p -> {
            scanFiles(false);
        });
        
        gbc = d.makeGbc(1, 3, 1, 1, GridBagConstraints.EAST);
        JButton rescanButton = new JButton("Rescan folder");
        rescanButton.setMargin(SMALL_BUTTON_INSETS);
        rescanButton.addActionListener(e -> {
            scanFiles(true);
        });
        rescanButton.setActionCommand("scanFolder");
        soundSettings.add(rescanButton, gbc);
        
        gbc = d.makeGbc(0, 3, 1, 1, GridBagConstraints.EAST);
        gbc.weightx = 1;
        soundSettings.add(filesResult, gbc);
        
        //---------------
        // Output Device
        //---------------
        JPanel devicePanel = new JPanel();
        devicePanel.add(new JLabel("Output Device: "));
        
        Map<String, String> devicePresets = new HashMap<>();
        devicePresets.put("", "<default>");
        for (String dev : Sound.getDeviceNames()) {
            devicePresets.put(dev, dev);
        }
        final ComboStringSetting device = new ComboStringSetting(devicePresets);
        device.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Sound.setDeviceName(device.getSettingValue());
            }
        });
        d.addStringSetting("soundDevice", device);
        devicePanel.add(device);
        gbc = d.makeGbc(0, 4, 2, 1);
        soundSettings.add(devicePanel, gbc);
        
        //======
        // Tabs
        //======
        JPanel notificationsPanel = addTitledPanel("Notifications", 0, true);
        
        JTabbedPane tabs = new JTabbedPane();
        gbc = GuiUtil.makeGbc(0, 0, 2, 1);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        notificationsPanel.add(tabs, gbc);

        editor.setPreferredSize(new Dimension(10,260));
        tabs.add("Events", editor);
        tabs.add("Notification Settings", GuiUtil.northWrap(notificationSettings));
        tabs.add("Sound Settings (Muted)", GuiUtil.northWrap(soundSettings));

        soundsEnabled.addItemListener(e -> {
            if (soundsEnabled.isSelected()) {
                tabs.setTitleAt(2, "Sound Settings");
            } else {
                tabs.setTitleAt(2, "Sound Settings (Muted)");
            }
        });
        
        gbc = GuiUtil.makeGbc(0, 1, 2, 1, GridBagConstraints.WEST);
        notificationsPanel.add(new JLabel("Tip: Double-click on Sound column to "
                + "directly open on the 'Sound' tab."), gbc);
        
        //=======
        // Other
        //=======
        JPanel follows = addTitledPanel("Followed Streams", 2);
        gbc = d.makeGbc(0, 0, 1, 1, GridBagConstraints.WEST);
        requestFollowedStreams = d.addSimpleBooleanSetting("requestFollowedStreams",
                "Request followed streams", "Allows Chatty to know "
                        + "about live streams you follow to notify you and "
                        + "display a list of them");
        follows.add(requestFollowedStreams, gbc);
        
        gbc = d.makeGbc(1, 0, 1, 1, GridBagConstraints.WEST);
        userReadPermission = new LinkLabel("", d.getLinkLabelListener());
        follows.add(userReadPermission, gbc);
        
        
        updateSettingsState();
    }
    
    protected void setUserReadPermission(boolean enabled) {
        if (enabled) {
            userReadPermission.setText("Required access available. ([help:followed ?])");
        } else {
            userReadPermission.setText("User read access required. ([help:followed ?])");
        }
        requestFollowedStreams.setEnabled(enabled);
        
    }
    
    private void updateSettingsState() {
        boolean enabled = nType.getSettingValue().equals(Long.valueOf(0));
        nPosition.setEnabled(enabled);
        nScreen.setEnabled(enabled);
        nDisplayTime.setEnabled(enabled);
        nMaxDisplayTime.setEnabled(enabled);
        userActivity.setEnabled(enabled);
    }
    
    protected void setData(List<Notification> data) {
        editor.setData(data);
    }
    
    protected List<Notification> getData() {
        return editor.getData();
    }
    
    
    
    protected void scanFiles(boolean showMessage) {
        
        Path path = soundsPath.getCurrentPath();
        System.out.println("scan Files "+path);
        File file = path.toFile();
        File[] files = file.listFiles(new WavFilenameFilter());
        String resultText = "";
        String warningText = "";
        if (files == null) {
            resultText = "Error scanning folder.";
        } else {
            if (files.length == 0) {
                resultText = "No sound files found.";
            } else {
                resultText = files.length+" sound files found.";
            }
            String[] fileNames = new String[files.length];
            for (int i=0;i<files.length;i++) {
                fileNames[i] = files[i].getName();
            }
            Arrays.sort(fileNames);
            editor.setSoundFiles(path, fileNames);
//            for (ComboStringSetting s : fileSettings.keySet()) {
//                Object selected = s.getSelectedItem();
//                s.removeAllItems();
//                boolean currentOneStillThere = false;
//                for (String item : fileNames) {
//                    if (item.equals(selected)) {
//                        currentOneStillThere = true;
//                    }
//                    s.add(item);
//                }
//                if (!currentOneStillThere && selected != null) {
//                    warningText += "\n'"+selected+"' (used as "+fileSettings.get(s)+" sound) wasn't found.";
//                } else {
//                    s.setSelectedItem(selected);
//                }
//            }
        }
        if (showMessage) {
            JOptionPane.showMessageDialog(this, resultText+warningText);
        }
        filesResult.setText(resultText);
    }
    
    private class MyButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("openFolder")) {
//                MiscUtil.openFolder(new File(FILE_PATH), d);
            } else if (e.getActionCommand().equals("scanFolder")) {
                scanFiles(true);
            }
        }
        
    }
    
    private static class WavFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            if (name.endsWith(".wav")) {
                return true;
            }
            return false;
        }
        
    }
    
    
}
