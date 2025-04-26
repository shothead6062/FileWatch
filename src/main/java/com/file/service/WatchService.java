package com.file.service;

import com.file.object.MonitorDataObject;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class WatchService {


    private static final Logger logger = Logger.getLogger(WatchService.class.getName());


    // 檔案快照儲存結構 (key為檔案路徑，value為上次修改時間)
    private static final Map<String, FileTime> fileTimeMap = new HashMap<>();

    // 日期時間格式化
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private MonitorDataObject dto;

    private TrayIcon trayIcon ;


    public WatchService(MonitorDataObject dto) {
        this.dto = dto;
    }

    /**
     * 檢查目錄中的變化
     */

    public void checkForChanges(String directoryMonitorPath) throws IOException {


        Path directory = Paths.get(directoryMonitorPath);

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            logger.warning("警告: 指定的目錄不存在或不可存取: " + directoryMonitorPath);
            return;
        }

        // 當前檔案集合
        Set<String> currentFiles = new HashSet<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, dto.fileExtension)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                if (fileName.startsWith(dto.monitorFileName)) {
                    String fullPath = path.toString();
                    currentFiles.add(fullPath);

                    FileTime lastModified = Files.getLastModifiedTime(path);

                    // 檢查是否為新檔案
                    if (!fileTimeMap.containsKey(fullPath)) {
                        String message = "發現新檔案: " + fileName + "\n" +
                                "建立時間: " + DATE_FORMAT.format(new Date(lastModified.toMillis())) + "\n" +
                                "檔案大小: " + formatFileSize(Files.size(path));

                        logger.info(getCurrentTime() + " - 發現新檔案: " + fileName);
                        logger.info("  建立時間: " + DATE_FORMAT.format(new Date(lastModified.toMillis())));
                        logger.info("  檔案大小: " + formatFileSize(Files.size(path)));

                        // 顯示Windows警示視窗
                        showWindowsAlert("檔案監控 - 新檔案", message);

                        fileTimeMap.put(fullPath, lastModified);
                    }
                    // 檢查檔案是否被修改
                    else if (!lastModified.equals(fileTimeMap.get(fullPath))) {
                        FileTime previousTime = fileTimeMap.get(fullPath);

                        String message = "檔案已修改: " + fileName + "\n" +
                                "上次修改時間: " + DATE_FORMAT.format(new Date(previousTime.toMillis())) + "\n" +
                                "目前修改時間: " + DATE_FORMAT.format(new Date(lastModified.toMillis())) + "\n" +
                                "檔案大小: " + formatFileSize(Files.size(path));

                        logger.info(getCurrentTime() + " - 檔案已修改: " + fileName);
                        logger.info("  上次修改時間: " + DATE_FORMAT.format(new Date(previousTime.toMillis())));
                        logger.info("  目前修改時間: " + DATE_FORMAT.format(new Date(lastModified.toMillis())));
                        logger.info("  檔案大小: " + formatFileSize(Files.size(path)));

                        // 顯示Windows警示視窗
                        showWindowsAlert("檔案監控 - 檔案已修改", message);

                        fileTimeMap.put(fullPath, lastModified);
                    }
                }
            }
        }

        // 檢查是否有檔案被刪除
        Set<String> deletedFiles = new HashSet<>(fileTimeMap.keySet());
        deletedFiles.removeAll(currentFiles);

        for (String deletedFile : deletedFiles) {
            Path path = Paths.get(deletedFile);
            String fileName = path.getFileName().toString();

            String message = "檔案已刪除: " + fileName;

            logger.info(getCurrentTime() + " - 檔案已刪除: " + fileName);

            // 顯示Windows警示視窗
            showWindowsAlert("檔案監控 - 檔案已刪除", message);

            fileTimeMap.remove(deletedFile);
        }
    }

    /**
     * 初始化檔案快照，記錄所有符合條件的檔案及其修改時間
     */
    public void initializeFileSnapshot(String dirPath) throws IOException {
        fileTimeMap.clear();

        Path directory = Paths.get(dirPath);
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, dto.fileExtension)) {
                for (Path path : stream) {
                    String fileName = path.getFileName().toString();
                    if (fileName.startsWith(dto.monitorFileName)) {
                        FileTime lastModified = Files.getLastModifiedTime(path);
                        fileTimeMap.put(path.toString(), lastModified);

                        logger.info("初始化: 找到檔案 " + fileName +
                                ", 最後修改時間: " + DATE_FORMAT.format(new Date(lastModified.toMillis())));
                    }
                }
            }
        } else {
            logger.warning("指定的目錄不存在或不可存取: " + dirPath);
        }
    }


    /**
     * 獲取當前時間的格式化字串
     */
    private static String getCurrentTime() {
        return DATE_FORMAT.format(new Date());
    }


    /**
     * 格式化檔案大小
     */
    private static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }


    /**
     * 顯示Windows警示視窗（同時支援對話框和系統托盤通知）
     */
    public void showWindowsAlert(final String title, final String message) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                // 顯示系統托盤通知（如果可用）
                if (trayIcon != null) {
                    trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
                }

                // 同時顯示彈出對話框（始終顯示，確保用戶看到通知）
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
            }
        });
    }

}
