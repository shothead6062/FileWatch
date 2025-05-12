# FileWatch Development Guidelines

This document provides guidelines for developing and maintaining the FileWatch application, a file monitoring tool that watches for changes in specified directories.

## Build/Configuration Instructions

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- Maven 3.6 or higher

### Building the Application
1. Clone the repository
2. Navigate to the project root directory
3. Build the project using Maven:
   ```bash
   mvn clean package
   ```
4. The built JAR file will be located in the `target` directory

### Running the Application
Run the application using the following command:
```bash
java -jar target/FileWatch-1.0-SNAPSHOT.jar
```

### Configuration
The application currently uses hardcoded configuration values in the `NetworkDirectoryMonitor` class:
- Monitored directory: `\\pf03\核心系統盤點共用資料夾`
- File name pattern: `差異分析訪談時間表`
- File extension: `*.xlsx`

These values can be changed at runtime through the system tray icon's settings menu.

## Testing Information

### Running Tests
The project uses JUnit 5 for testing. To run all tests:
```bash
mvn test
```

To run a specific test class:
```bash
mvn test -Dtest=WatchServiceTest
```

To run a specific test method:
```bash
mvn test -Dtest=WatchServiceTest#testInitializeFileSnapshot
```

### Adding New Tests
1. Create test classes in the `src/test/java` directory, following the same package structure as the main code
2. Use JUnit 5 annotations (`@Test`, `@BeforeEach`, etc.) to define test methods
3. Use the `@TempDir` annotation to create temporary directories for file-based tests
4. Add debug logging with the `[DEBUG_LOG]` prefix for better visibility during test runs

### Test Example
Here's a simple test for the `FileWatchService` class:

```java
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
}
```

## Additional Development Information

### Code Structure
The application follows a simple structure:
- `com.file.object`: Data objects (e.g., `MonitorDataObject`)
- `com.file.service`: Services (e.g., `FileWatchService`)
- `com.file.ui`: UI components (e.g., `MonitorUICreates`)
- `com.file.watch`: Core monitoring functionality (e.g., `NetworkDirectoryMonitor`, `MonitorScheduler`)

### Known Issues and Improvement Areas
Refer to `docs/tasks.md` for a comprehensive list of improvement tasks. Key areas include:
1. Proper dependency injection instead of static initialization
2. Improved configuration management
3. Refactoring to use Java's built-in WatchService API
4. Better error handling and recovery
5. Improved logging system
6. Code-level improvements (encapsulation, naming conventions, etc.)

### Debugging
The application uses Java's built-in logging system. Logs are stored in the `logs` directory with daily rolling files:
```
logs/monitor_yyyy-MM-dd.log
```

### UI Components
The application uses a system tray icon with a popup menu for user interaction. The UI is created in the `MonitorUICreates` class.

### Monitoring Logic
The core monitoring logic is in the `FileWatchService` class, which:
1. Initializes a snapshot of files in the monitored directory
2. Periodically checks for changes (new files, modified files, deleted files)
3. Logs changes and displays notifications

The monitoring is scheduled by the `MonitorScheduler` class, which runs the check every 10 seconds.
