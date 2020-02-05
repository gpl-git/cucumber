// Created by Viacheslav (Slava) Skryabin 04/01/2018
package support;

import cucumber.api.Scenario;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestContext {

    private static WebDriver driver;

    private static Map<String, Object> testData = new HashMap<>();

    public static void setTestData(String key, Object value) {
        testData.put(key, value);
    }

    public static String getStringTestData(String key) {
        return (String) testData.get(key);
    }

    public static Integer getIntegerTestData(String key) {
        return (Integer) testData.get(key);
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static File getFile(String fileName, String extension) {
        String path = System.getProperty("user.dir") + "/src/test/resources/data/" + fileName + "." + extension;
        return new File(path);
    }

    public static void saveData(String fileName, String extension, byte[] bytes) {
        try(FileOutputStream stream = new FileOutputStream(getFile(fileName, extension))) {
            stream.write(bytes);
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static YamlMap getConfig() {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(getFile("config", "yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new YamlMap(new Yaml().load(stream));
    }

    public static Map<String, String> getData(String fileName) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(getFile(fileName, "yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Yaml().load(stream);
    }

    public static Map<String, String> getCustomer() {
        return getData("customer");
    }

    public static String addTimeStampToEmail(String email) {
        String name = email.split("@")[0];
        String domain = email.split("@")[1];
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-sss").format(new Date());
        return name + "+" + timestamp + "@" + domain;
    }

    public static String addTimeStampToField(String field) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-sss").format(new Date());
        return field + "-" + timestamp;
    }

    public static JavascriptExecutor getExecutor() {
        return (JavascriptExecutor) driver;
    }

    public static WebDriverWait getWait() {
        return getWait(getConfig().getInt("explicitTimeout"));
    }

    public static WebDriverWait getLongWait() {
        return getWait(getConfig().getInt("explicitLongTimeout"));
    }

    public static WebDriverWait getWait(int timeout) {
        return new WebDriverWait(driver, timeout);
    }

    public static Actions getActions() {
        return new Actions(driver);
    }

    public static void initialize() {
        initialize(getConfig().getString("browser"), getConfig().getBoolean("isHeadless"));
    }

    public static void teardown() {
        driver.quit();
    }

    public static void initialize(String browser, boolean isHeadless) {
        String osName = System.getProperty("os.name");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        URL hubUrl = null;

        switch (getConfig().getString("infrastructure")) {
            case "grid":
                capabilities.setBrowserName(browser);
                capabilities.setPlatform(Platform.ANY);
                try {
                    hubUrl = new URL("http://" + getConfig().getString("hubUrl") + ":4446/wd/hub");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                driver = new RemoteWebDriver(hubUrl, capabilities);
                break;
            case "selenoid":
                capabilities.setBrowserName(browser);
                capabilities.setPlatform(Platform.LINUX);
                capabilities.setVersion(getConfig().getDouble("browserVersion").toString());
                capabilities.setCapability("enableVNC", true);
                capabilities.setCapability("enableVideo", true);
//                capabilities.setCapability("sessionTimeout", "5m");
//                capabilities.setCapability("idleTimeout", "300");
//                capabilities.setCapability("videoScreenSize", "1920x1080");
//                capabilities.setCapability("screenResolution", "1920x1080");

                String testName = getConfig().getString("env") + " " + getStringTestData("scenarioTag") + " \"" + getStringTestData("scenarioName") + "\"";
                capabilities.setCapability("name", testName);
                String videoName = String.join("", testName.replaceAll("[^A-Za-z0-9]", "_"), "_", String.valueOf(new Date().getTime()), ".mp4");
                setTestData("videoName", videoName);
                capabilities.setCapability("videoName", videoName);

                try {
                    hubUrl = new URL("http://" + getConfig().getString("hubUrl") + ":4444/wd/hub");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                driver = new RemoteWebDriver(hubUrl, capabilities);
                break;
            default:
                switch (browser) {
                    case "chrome":
                        String chromeDriverName = "chromedriver.exe";
                        if (osName != null && (osName.contains("Mac") || osName.contains("Linux"))) {
                            chromeDriverName = "chromedriver";
                        }
                        System.setProperty("webdriver.chrome.driver", getDriversDirPath() + chromeDriverName);
                        Map<String, Object> chromePreferences = new HashMap<>();
                        chromePreferences.put("profile.default_content_settings.geolocation", 2);
                        chromePreferences.put("download.prompt_for_download", false);
                        chromePreferences.put("download.directory_upgrade", true);
                        chromePreferences.put("download.default_directory", getDownloadsPath());
                        chromePreferences.put("credentials_enable_service", false);
                        chromePreferences.put("password_manager_enabled", false);
                        chromePreferences.put("safebrowsing.enabled", "true");
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.addArguments("--start-maximized");
                        chromeOptions.setExperimentalOption("prefs", chromePreferences);
//                chromeOptions.addExtensions(new File(System.getProperty("user.dir")+ "/src/test/resources/extensions/ChroPath.crx"));
                        if (isHeadless) {
                            chromeOptions.setHeadless(true);
                            chromeOptions.addArguments("--window-size=1920,1080");
                            chromeOptions.addArguments("--disable-gpu");
                        }
                        driver = new ChromeDriver(chromeOptions);
                        break;
                    case "firefox":
                        String geckoDriverName = "geckodriver.exe";
                        if (osName != null && (osName.contains("Mac") || osName.contains("Linux"))) {
                            geckoDriverName = "geckodriver";
                        }
                        System.setProperty("webdriver.gecko.driver", getDriversDirPath() + geckoDriverName);
                        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
                        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
                        FirefoxProfile firefoxProfile = new FirefoxProfile();
                        firefoxProfile.setPreference("xpinstall.signatures.required", false);
                        firefoxProfile.setPreference("app.update.enabled", false);
                        firefoxProfile.setPreference("browser.download.folderList", 2);
                        firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
                        firefoxProfile.setPreference("browser.download.dir", getDownloadsPath());
                        firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip;application/octet-stream;application/x-zip;application/x-zip-compressed;text/css;text/html;text/plain;text/xml;text/comma-separated-values");
                        firefoxProfile.setPreference("browser.helperApps.neverAsk.openFile", "application/zip;application/octet-stream;application/x-zip;application/x-zip-compressed;text/css;text/html;text/plain;text/xml;text/comma-separated-values");
                        firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
                        firefoxProfile.setPreference("plugin.disable_full_page_plugiâ€Œâ€‹n_for_types", "application/pdf,application/vnd.adobe.xfdf,application/vnd.â€Œâ€‹fdf,application/vnd.â€Œâ€‹adobe.xdp+xml");
                        firefoxProfile.setPreference("webdriver.log.driver", "OFF");
                        FirefoxOptions firefoxOptions = new FirefoxOptions().setProfile(firefoxProfile).setLogLevel(FirefoxDriverLogLevel.INFO);
                        if (isHeadless) {
                            FirefoxBinary firefoxBinary = new FirefoxBinary();
                            firefoxBinary.addCommandLineOptions("--headless");
                            firefoxOptions.setBinary(firefoxBinary);
                        }
                        driver = new FirefoxDriver(firefoxOptions);
                        break;
                    case "edge":
                        System.setProperty("webdriver.edge.driver", getDriversDirPath() + "MicrosoftWebDriver.exe");
                        driver = new EdgeDriver();
                        break;
                    case "ie":
                        System.setProperty("webdriver.ie.driver", getDriversDirPath() + "IEDriverServer.exe");
                        DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                        ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                        ieCapabilities.setCapability("requireWindowFocus", true);
                        InternetExplorerOptions ieOptions = new InternetExplorerOptions(ieCapabilities);
                        driver = new InternetExplorerDriver(ieOptions);
                        break;
                    default:
                        throw new RuntimeException("Driver is not implemented for: " + browser);
                }
        }
    }

    private static String getDriversDirPath() {
        return System.getProperty("user.dir") + String.format("%1$ssrc%1$stest%1$sresources%1$sdrivers%1$s", File.separator);
    }

    private static String getDownloadsPath() {
        return System.getProperty("user.dir") + String.format("%1$ssrc%1$stest%1$sresources%1$sdownloads%1$s", File.separator);
    }

    public static void setScenarioData(Scenario scenario) {
        TestContext.setTestData("scenarioName", scenario.getName());
        for (String tag : scenario.getSourceTagNames()) {
            Matcher matcher = Pattern.compile("@[\\w_]+\\d+").matcher(tag);
            if (matcher.find()) {
                TestContext.setTestData("scenarioTag", matcher.group(0));
                break;
            }
        }
    }
}