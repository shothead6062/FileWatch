package com.file.object;

/**
 * Data object for file monitoring configuration.
 * Contains the directory path, file extension, and file name pattern to monitor.
 */
public class MonitorDataObject {

    private String directoryMonitorPath;
    private String fileExtension;
    private String monitorFileName;

    /**
     * Default constructor
     */
    public MonitorDataObject() {
    }

    /**
     * Constructor with all parameters
     * 
     * @param directoryMonitorPath the path to the directory to monitor
     * @param fileExtension the file extension to monitor (e.g., "*.xlsx")
     * @param monitorFileName the file name pattern to monitor
     */
    public MonitorDataObject(String directoryMonitorPath, String fileExtension, String monitorFileName) {
        setDirectoryMonitorPath(directoryMonitorPath);
        setFileExtension(fileExtension);
        setMonitorFileName(monitorFileName);
    }

    /**
     * Get the directory monitor path
     * 
     * @return the directory monitor path
     */
    public String getDirectoryMonitorPath() {
        return directoryMonitorPath;
    }

    /**
     * Set the directory monitor path
     * 
     * @param directoryMonitorPath the directory monitor path to set
     * @throws IllegalArgumentException if the path is null or empty
     */
    public void setDirectoryMonitorPath(String directoryMonitorPath) {
        if (directoryMonitorPath == null || directoryMonitorPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory monitor path cannot be null or empty");
        }
        this.directoryMonitorPath = directoryMonitorPath;
    }

    /**
     * Get the file extension
     * 
     * @return the file extension
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Set the file extension
     * 
     * @param fileExtension the file extension to set
     * @throws IllegalArgumentException if the file extension is null or empty
     */
    public void setFileExtension(String fileExtension) {
        if (fileExtension == null || fileExtension.trim().isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be null or empty");
        }
        this.fileExtension = fileExtension;
    }

    /**
     * Get the monitor file name
     * 
     * @return the monitor file name
     */
    public String getMonitorFileName() {
        return monitorFileName;
    }

    /**
     * Set the monitor file name
     * 
     * @param monitorFileName the monitor file name to set
     * @throws IllegalArgumentException if the monitor file name is null or empty
     */
    public void setMonitorFileName(String monitorFileName) {
        if (monitorFileName == null || monitorFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Monitor file name cannot be null or empty");
        }
        this.monitorFileName = monitorFileName;
    }
}
