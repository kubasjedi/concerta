package org.example;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class SelenideConfig {

    public static void setup() {
        Configuration.browser = "chrome";
        Configuration.headless = true; // Run Chrome in headless mode (no UI)
        Configuration.browserSize = "1366x768"; // Consistent window size for headless

        ChromeOptions options = new ChromeOptions();

        // Essential arguments for headless Chrome in CI environments
        options.addArguments("--no-sandbox"); // Required for Chrome in Docker/CI without root privileges
        options.addArguments("--disable-gpu"); // Recommended for headless
        options.addArguments("--window-size=1366,768"); // Matches browserSize
        options.addArguments("--proxy-bypass-list=<-loopback>");

        // Handle the user data directory issue
        try {
            Path userDataDir = Files.createTempDirectory("chrome-user-data-" + UUID.randomUUID().toString());
            options.addArguments("--user-data-dir=" + userDataDir.toAbsolutePath().toString());
            // Add a shutdown hook to clean up the temporary directory
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(userDataDir)
                            .sorted(java.util.Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    System.out.println("Cleaned up temporary user data directory: " + userDataDir);
                } catch (Exception e) {
                    System.err.println("Failed to delete user data directory " + userDataDir + ": " + e.getMessage());
                }
            }));
        } catch (Exception e) {
            System.err.println("Failed to create temporary user data directory: " + e.getMessage());
            // Fallback or rethrow if this is a critical failure
        }

        // Your existing preferences
        options.setExperimentalOption("prefs", new HashMap<String, Object>() {{
            put("autofill.credit_card_enabled", false);
            put("autofill.profile_enabled", false);
            put("credentials_enable_service", false);
            put("download.default_directory", "/home/runner/work/concerta/downloads"); // Ensure this path exists and is writable
            put("plugins.always_open_pdf_externally", true);
            put("profile.default_content_setting_values.automatic_downloads", 1);
            put("profile.password_manager_enabled", false);
            put("profile.password_manager_leak_detection", false);
            put("safeBrowse.enabled", true);
        }});

        // Exclude problematic switches
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "load-extension"});
        // Selenide automatically finds chromedriver, so you usually don't need to set the binary explicitly
        // If /usr/bin/google-chrome is indeed the correct path on the ubuntu-latest runner, you can keep this
        // options.setBinary("/usr/bin/google-chrome");

        Configuration.browserCapabilities = options;
        Configuration.pageLoadStrategy = "normal";
        Configuration.timeout = 15000; // Increase timeout for page loading
        Configuration.baseUrl = ""; // No base URL needed if you open full URLs
        // You might want to increase this if the page takes longer to load
        Configuration.pageLoadTimeout = 30000; // Increase page load timeout
    }
}