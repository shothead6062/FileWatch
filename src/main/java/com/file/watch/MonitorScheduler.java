package com.file.watch;

import com.file.object.MonitorDataObject;
import com.file.service.FileWatchService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitorScheduler {

    private static final Logger logger = Logger.getLogger(MonitorScheduler.class.getName());

    // 排程執行器
    private static ScheduledExecutorService scheduler;

    // 監視間隔時間（秒）
    private static final int MONITOR_INTERVAL_SECONDS = 10;

    private MonitorDataObject dto;

    private FileWatchService watchService;

    public MonitorScheduler(MonitorDataObject dto, FileWatchService watchService) {
        this.dto = dto;
        this.watchService = watchService;
    }

    public void startScheduled() {

        logger.info("開始監控網路資料夾: " + dto.getDirectoryMonitorPath());
        logger.info("監控條件: 副檔名為 " + dto.getFileExtension() + " 且檔名以「"+ dto.getMonitorFileName()+ "」開頭");
        logger.info("監控頻率: 每 " + MONITOR_INTERVAL_SECONDS + " 秒檢查一次");
        logger.info("===========================================");


        scheduler = Executors.newScheduledThreadPool(1);

        // 創建監控任務
        Runnable monitorTask = new Runnable() {
            @Override
            public void run() {
                try {
                    watchService.checkForChanges(dto.getDirectoryMonitorPath());
                } catch (Exception e) {
                    logger.log(Level.WARNING, "監控過程發生錯誤", e);
                }
            }
        };

        // 排程任務 - 先延遲1秒後開始，然後每MONITOR_INTERVAL_SECONDS秒執行一次
        scheduler.scheduleAtFixedRate(monitorTask, 1, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);


        // 註冊關閉鉤子，確保優雅關閉
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("正在關閉監控程式...");
                shutdownMonitor();
                logger.info("監控程式已關閉");
            }
        });

        // 保持主線程活著
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("主線程被中斷");
            shutdownMonitor();
        }

    }

    /**
     * 關閉監控程式
     */
    public void shutdownMonitor() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                // 等待最多30秒讓任務完成
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }



}
