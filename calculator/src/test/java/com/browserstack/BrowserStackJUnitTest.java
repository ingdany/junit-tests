package com.browserstack;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BrowserStackJUnitTest {
    public WebDriver driver;
    public static String userName, accessKey;
    public static Map<String, Object> browserStackYamlMap;
    public static final String USER_DIR = "user.dir";

    public BrowserStackJUnitTest() {
        File file = new File(getUserDir() + "/browserstack.yml");
        this.browserStackYamlMap = convertYamlFileToMap(file, new HashMap<>());
    }

    @Before
    public void setUp() throws Exception {
        MutableCapabilities capabilities = new MutableCapabilities();
        userName = System.getenv("BROWSERSTACK_USERNAME") != null ? System.getenv("BROWSERSTACK_USERNAME") : (String) browserStackYamlMap.get("userName");
        accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY") != null ? System.getenv("BROWSERSTACK_ACCESS_KEY") : (String) browserStackYamlMap.get("accessKey");
        this.driver = new RemoteWebDriver(
                new URL(String.format("https://%s:%s@hub.browserstack.com/wd/hub", userName , accessKey)), capabilities);
    }

    public static void mark(WebDriver remoteDriver, String status, String reason) {
        final JavascriptExecutor jse = (JavascriptExecutor) remoteDriver;
        JSONObject executorObject = new JSONObject();
        JSONObject argumentsObject = new JSONObject();
        argumentsObject.put("status", status);
        argumentsObject.put("reason", reason);
        executorObject.put("action", "setSessionStatus");
        executorObject.put("arguments", argumentsObject);
        jse.executeScript(String.format("browserstack_executor: %s", executorObject));
    }

    @After
    public void tearDown() {
        this.driver.quit();
    }

    private String getUserDir() {
        return System.getProperty(USER_DIR);
    }

    private Map<String, Object> convertYamlFileToMap(File yamlFile, Map<String, Object> map) {
        try {
            InputStream inputStream = Files.newInputStream(yamlFile.toPath());
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(inputStream);
            map.putAll(config);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Malformed browserstack.yml file - %s.", e));
        }
        return map;
    }
}
