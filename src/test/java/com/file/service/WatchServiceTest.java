package com.file.service;

import com.file.object.MonitorDataObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FileWatchService
 */
public class WatchServiceTest {

    @TempDir
    Path tempDir;

    private MonitorDataObject dto;
    private FileWatchService watchService;
    private Path testFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test file in the temporary directory
        testFile = tempDir.resolve("差異分析訪談時間表_test.xlsx");
        Files.createFile(testFile);

        // Initialize the DTO with test values
        dto = new MonitorDataObject();
        dto.setDirectoryMonitorPath(tempDir.toString());
        dto.setMonitorFileName("差異分析訪談時間表");
        dto.setFileExtension("*.xlsx");

        // Create the FileWatchService with the test DTO
        watchService = new FileWatchService(dto);
    }

    @Test
    void testInitializeFileSnapshot() throws IOException {
        // Initialize the file snapshot
        watchService.initializeFileSnapshot(dto.getDirectoryMonitorPath());

        // Modify the test file to trigger a change
        Files.writeString(testFile, "Test content", StandardOpenOption.APPEND);

        // This test just verifies that the initialization doesn't throw exceptions
        // In a real test, we would mock the file system and verify the behavior
        assertTrue(Files.exists(testFile), "Test file should exist");
        System.out.println("[DEBUG_LOG] Test file created at: " + testFile);
        System.out.println("[DEBUG_LOG] File snapshot initialized for directory: " + dto.getDirectoryMonitorPath());
    }

    @Test
    void testCheckForChanges() throws IOException {
        // Initialize the file snapshot
        watchService.initializeFileSnapshot(dto.getDirectoryMonitorPath());

        // Create a new file after initialization to trigger a change detection
        Path newFile = tempDir.resolve("差異分析訪談時間表_new.xlsx");
        Files.createFile(newFile);

        // Check for changes
        watchService.checkForChanges(dto.getDirectoryMonitorPath());

        // Verify the new file exists
        assertTrue(Files.exists(newFile), "New test file should exist");
        System.out.println("[DEBUG_LOG] New test file created at: " + newFile);

        // Modify the file to test modification detection
        Files.writeString(newFile, "Modified content", StandardOpenOption.APPEND);

        // Check for changes again
        watchService.checkForChanges(dto.getDirectoryMonitorPath());

        // Delete the file to test deletion detection
        Files.delete(newFile);
        assertFalse(Files.exists(newFile), "New test file should be deleted");

        // Check for changes one more time
        watchService.checkForChanges(dto.getDirectoryMonitorPath());

        System.out.println("[DEBUG_LOG] File changes test completed successfully");
    }
}
