package org.example;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;

import static com.codeborne.selenide.Configuration.browserCapabilities;

public class SelenideConfig {

    public static void setup() {
        Configuration.browser = "chrome";
        Configuration.headless = true; // Run Chrome in headless mode (no UI)
        Configuration.browserSize = "1920x1080"; // Consistent window size for headless
//        ((ChromeOptions) browserCapabilities).setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation")); // Another anti-detection measure

        ChromeOptions options = new ChromeOptions();

        // Essential arguments for headless Chrome in CI environments
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
        options.addArguments("--no-sandbox"); // Required for Chrome in Docker/CI without root privileges
        options.addArguments("--disable-gpu"); // Recommended for headless
        options.addArguments("--window-size=1366,768"); // Matches browserSize
        options.addArguments("--proxy-bypass-list=<-loopback>");
        options.addArguments("--disable-blink-features=AutomationControlled"); // Hide automation detection


        // Exclude problematic switches
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "load-extension"});
        browserCapabilities = options;
        // Selenide automatically finds chromedriver, so you usually don't need to set the binary explicitly
        // If /usr/bin/google-chrome is indeed the correct path on the ubuntu-latest runner, you can keep this
        // options.setBinary("/usr/bin/google-chrome");

        Configuration.pageLoadStrategy = "normal";
        Configuration.timeout = 15000; // Increase timeout for page loading
        Configuration.baseUrl = ""; // No base URL needed if you open full URLs
        // You might want to increase this if the page takes longer to load
        Configuration.pageLoadTimeout = 30000; // Increase page load timeout
    }
}