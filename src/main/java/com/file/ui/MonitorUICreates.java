package com.file.ui;

import com.file.object.MonitorDataObject;
import com.file.service.FileWatchService;
import com.file.watch.MonitorScheduler;
import com.file.watch.NetworkDirectoryMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 設置系統托盤圖示和相關選單
 */
public class MonitorUICreates {

    private static final Logger logger = Logger.getLogger(MonitorUICreates.class.getName());

    private MonitorDataObject dto;

    private MonitorScheduler scheduler;

    private FileWatchService watchService;


    public MonitorUICreates(MonitorDataObject dto, MonitorScheduler scheduler, FileWatchService watchService) {
        this.dto = dto;
        this.scheduler = scheduler;
        this.watchService = watchService;
    }

    public void setupTrayIcon() throws AWTException {

        // 檢查系統是否支援系統托盤
        if (!SystemTray.isSupported()) {
            throw new RuntimeException("系統初始化失敗。");
        }

        Image image = null;

        try {
            SystemTray tray = SystemTray.getSystemTray();
            URL iconURL = NetworkDirectoryMonitor.class.getClassLoader().getResource("icon.png");
            image = Toolkit.getDefaultToolkit().getImage(iconURL);
            logger.info("已成功載入自定義圖示: " + iconURL);
        }catch (Exception e) {
            logger.log(Level.WARNING,"載入圖示時發生錯誤: ",e);
            // 如果無法載入任何圖示，創建一個16x16像素的空白圖示（更明顯）
            image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
        }

        // 設定右下角程式縮圖
        final TrayIcon trayIcon = new TrayIcon(image, "檔案監控服務");
        trayIcon.setImageAutoSize(true);

        JPopupMenu trayPopup = createPopupMenu();

        // 添加滑鼠事件監聽器以顯示JPopupMenu
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // 獲取托盤圖示的位置
                    Point point = e.getPoint();

                    // 計算選單顯示位置 (向上顯示，讓選單在托盤圖示上方)
                    // 注意：取得滑鼠位置需要轉換座標系
                    Point location = e.getLocationOnScreen();

                    // 顯示選單在滑鼠位置
                    trayPopup.setLocation(location.x, location.y);
                    trayPopup.setInvoker(trayPopup);
                    trayPopup.setVisible(true);
                }
            }
        });

        SystemTray tray = SystemTray.getSystemTray();
        tray.add(trayIcon);


    }

    private JPopupMenu createPopupMenu() {

        final JPopupMenu trayPopup = new JPopupMenu();

        trayPopup.add(createSettingMenuItem());
        trayPopup.addSeparator();
        trayPopup.add(createCloseMenuItem());
        trayPopup.addSeparator();
        trayPopup.add(createAboutMenu());


        return trayPopup;
    }

    private JMenuItem createAboutMenu() {

        // 添加關於選項
        JMenuItem aboutItem = new JMenuItem("關於");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "網路資料夾監控程式\n" +
                                "作者：Clarke Yeh\n" +
                                "版本: 1.0",
                        "關於", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return aboutItem;
    }


    private JMenuItem createCloseMenuItem() {
        // 添加關閉程式選項
        JMenuItem exitItem = new JMenuItem("關閉程式");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("透過系統 托盤選單關閉程式...");
                scheduler.shutdownMonitor();
                System.exit(0);
            }
        });

        return exitItem;
    }


    private JMenuItem createSettingMenuItem() {

        // 創建文本輸入框
        JTextField folderPathTextField = new JTextField(dto.getDirectoryMonitorPath(),20);
        JTextField fileExtensionTextField = new JTextField(dto.getFileExtension(),10);
        JTextField fileNameTextField = new JTextField(dto.getMonitorFileName(),20);

        // 創建標籤
        JLabel folderPathLabel = new JLabel("監控資料夾位置:");
        JLabel fileExtensionLabel = new JLabel("監控副檔名:");
        JLabel fileNameLabel = new JLabel("監控檔案名稱:");

        // 創建面板並設置佈局
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(3, 2, 5, 10)); // 3行2列，水平間距5，垂直間距10

        // 添加組件到面板
        myPanel.add(folderPathLabel);
        myPanel.add(folderPathTextField);
        myPanel.add(fileExtensionLabel);
        myPanel.add(fileExtensionTextField);
        myPanel.add(fileNameLabel);
        myPanel.add(fileNameTextField);

        // 添加菜單項
        JMenuItem displayItem = new JMenuItem("監控設定");
        displayItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String beforeChangePath = dto.getDirectoryMonitorPath();

                int result = JOptionPane.showConfirmDialog(null, myPanel, "監控設定",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {

                    // 獲取用戶輸入的值
                    dto.setDirectoryMonitorPath(folderPathTextField.getText());
                    dto.setFileExtension(fileExtensionTextField.getText());
                    dto.setMonitorFileName(fileNameTextField.getText());

                    logger.info("Path Is Change \n" +
                            "Before Change - " + beforeChangePath + "\n" +
                            "After Change - " + dto.getDirectoryMonitorPath());

                    try {
                        watchService.initializeFileSnapshot(dto.getDirectoryMonitorPath());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }
        });

        return displayItem;
    }






}
