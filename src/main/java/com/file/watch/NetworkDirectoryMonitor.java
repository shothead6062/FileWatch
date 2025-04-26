package com.file.watch;


import com.file.object.MonitorDataObject;
import com.file.service.WatchService;
import com.file.ui.MonitorUICreates;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * 網路資料夾監控程式 (使用排程任務)
 * 功能：監控指定網路資料夾，當有新檔案加入或檔案修改時，印出相關紀錄並顯示Windows警示視窗
 * 支援條件：
 * - 僅監控.xlsx檔案
 * - 檔名以「差異分析訪談時間表」開頭的檔案
 */
public class NetworkDirectoryMonitor {

    private static final MonitorDataObject dto;

    private static final MonitorUICreates uiCreater;

    private static final MonitorScheduler scheduler;

    private static final WatchService watchService;



    // 日誌記錄器
    private static final Logger logger = Logger.getLogger(NetworkDirectoryMonitor.class.getName());

    static {
            // 設置日誌記錄器
            // 確保logs目錄存在
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                try {
                    Files.createDirectories(logsDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // 設置日誌檔案處理器 - 使用每日滾動的日誌檔案
            // 檔案格式: logs/monitor_yyyy-MM-dd.log
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String logFileName = "logs/monitor_" + dateFormat.format(new Date()) + ".log";

            // 設置日誌檔案處理器，true表示追加到檔案
            FileHandler fileHandler = null;
            try {
                fileHandler = new FileHandler(logFileName, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 使用簡單的格式化器，包含時間戳
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // 添加處理器到logger
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);

            // 同時也輸出到控制台
            logger.setUseParentHandlers(true);

            logger.info("===============================================");
            logger.info("檔案監控服務啟動於: " + new Date());
            logger.info("日誌檔案位置: " + Paths.get(logFileName).toAbsolutePath());
            logger.info("===============================================");


            //init DI
            dto = new MonitorDataObject();
            dto.directoryMonitorPath = "\\\\pf03\\核心系統盤點共用資料夾";
            dto.monitorFileName = "差異分析訪談時間表";
            dto.fileExtension = "*.xlsx";


            watchService = new WatchService(dto);
            scheduler = new MonitorScheduler(dto,watchService);
            uiCreater = new MonitorUICreates(dto,scheduler,watchService);

    }

    public static void main(String[] args) throws AWTException {


        // 設置系統托盤圖示 (如果支援)
        uiCreater.setupTrayIcon();

        // 設置Windows風格的UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warning("無法設置Windows風格UI: " + e.getMessage());
        }

        // 初始化檔案快照
        try {
            watchService.initializeFileSnapshot(dto.directoryMonitorPath);
            logger.info("初始檔案快照建立完成");
        } catch (Exception e) {
            logger.log(Level.WARNING, "建立初始檔案快照時發生錯誤", e);
            logger.info("程式將嘗試繼續執行...");
        }

        // 創建並配置排程執行器
        scheduler.startScheduled();


        logger.info("監控任務已啟動...");

    }


}