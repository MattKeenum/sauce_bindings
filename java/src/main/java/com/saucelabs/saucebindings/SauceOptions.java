package com.saucelabs.saucebindings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(chain = true)
@Setter @Getter
public class SauceOptions {
    @Setter(AccessLevel.NONE) private MutableCapabilities seleniumCapabilities;
    public TimeoutStore timeout = new TimeoutStore();

    // w3c settings - applies to any sauce session
    protected Browser browserName = Browser.CHROME;
    protected SaucePlatform platformName = SaucePlatform.WINDOWS_10;
    protected String browserVersion = "latest";

    // w3c settings - applies only to VDC
    private PageLoadStrategy pageLoadStrategy;
    private Boolean acceptInsecureCerts = null;
    private Proxy proxy;
    private Boolean setWindowRect = null;
    @Getter(AccessLevel.NONE) private Map<Timeouts, Integer> timeouts;
    private Boolean strictFileInteractability = null;
    private UnhandledPromptBehavior unhandledPromptBehavior;

    // Sauce Settings
    private Boolean avoidProxy = null;
    private String build;
    private Boolean capturePerformance = null;
    private String chromedriverVersion;
    private Integer commandTimeout = null;
    private Map<String, Object> customData = null;
    private Boolean extendedDebugging = null;
    private Integer idleTimeout = null;
    private String iedriverVersion;
    private Integer maxDuration = null;
    private String name;
    private String parentTunnel;
    private Map<Prerun, Object> prerun;
    private URL prerunUrl;
    private Integer priority = null;
    private JobVisibility jobVisibility; // the actual key for this is a Java reserved keyword "public"
    private Boolean recordLogs = null;
    private Boolean recordScreenshots = null;
    private Boolean recordVideo = null;
    private String screenResolution;
    private String seleniumVersion;
    private List<String> tags = null;
    private String timeZone;
    private String tunnelIdentifier;
    private Boolean videoUploadOnPass = null;

    public static final List<String> primaryEnum = List.of(
            "browserName",
            "jobVisibility",
            "pageLoadStrategy",
            "platformName",
            "timeouts",
            "unhandledPromptBehavior"
    );

    public static final List<String> secondaryEnum = List.of(
            "prerun",
            "timeouts"
    );

    public static final List<String> w3cDefinedOptions = List.of(
            "browserName",
            "browserVersion",
            "platformName",
            "pageLoadStrategy",
            "acceptInsecureCerts",
            "proxy",
            "setWindowRect",
            "timeouts",
            "strictFileInteractability",
            "unhandledPromptBehavior");

    public static final List<String> sauceDefinedOptions = List.of(
            "avoidProxy",
            "build",
            "capturePerformance",
            "chromedriverVersion",
            "commandTimeout",
            "customData",
            "extendedDebugging",
            "idleTimeout",
            "iedriverVersion",
            "maxDuration",
            "name",
            "parentTunnel",
            "prerun",
            "priority",
            // public, do not use, reserved keyword, using jobVisibility
            "recordLogs",
            "recordScreenshots",
            "recordVideo",
            "screenResolution",
            "seleniumVersion",
            "tags",
            "timeZone",
            "tunnelIdentifier",
            "videoUploadOnPass");

    public static final Map<String, String> knownCITools;
    static {
        knownCITools = new HashMap<>();
        knownCITools.put("Jenkins", "BUILD_TAG");
        knownCITools.put("Bamboo", "bamboo_agentId");
        knownCITools.put("Travis", "TRAVIS_JOB_ID");
        knownCITools.put("Circle", "CIRCLE_JOB");
        knownCITools.put("GitLab", "CI");
        knownCITools.put("TeamCity", "TEAMCITY_PROJECT_NAME");
    }

    public static SauceOptions headless() {
        SauceOptions headlessOptions = new SauceOptions();
        headlessOptions.setPlatformName(SaucePlatform.LINUX);
        return headlessOptions;
    }

    public SauceOptions() {
        this(new MutableCapabilities());
    }

    public SauceOptions(ChromeOptions options) {
        this(new MutableCapabilities(options));
    }

    public SauceOptions(EdgeOptions options) {
        this(new MutableCapabilities(options));
    }

    public SauceOptions(FirefoxOptions options) {
        this(new MutableCapabilities(options));
    }

    public SauceOptions(InternetExplorerOptions options) {
        this(new MutableCapabilities(options));
    }

    public SauceOptions(SafariOptions options) {
        this(new MutableCapabilities(options));
    }

    public Map<Timeouts, Integer> getTimeouts() {
        if (timeout.getTimeouts().isEmpty()) {
            return timeouts;
        }
        return timeout.getTimeouts();
    }

    protected SauceOptions(MutableCapabilities options) {
        seleniumCapabilities = new MutableCapabilities(options.asMap());
        if (options.getCapability("browserName") != null) {
            setCapability("browserName", options.getCapability("browserName"));
        }
    }

    public MutableCapabilities toCapabilities() {
        MutableCapabilities sauceCapabilities = new MutableCapabilities();
        addAuthentication(sauceCapabilities);

        if (getCapability("jobVisibility") != null) {
            sauceCapabilities.setCapability("public", getCapability("jobVisibility"));
        }

        if (getCapability("prerunUrl") != null) {
            sauceCapabilities.setCapability("prerun", getCapability("prerunUrl"));
        }

        w3cDefinedOptions.forEach((capability) -> {
            addCapabilityIfDefined(seleniumCapabilities, capability);
        });

        sauceDefinedOptions.forEach((capability) -> {
            addCapabilityIfDefined(sauceCapabilities, capability);
        });

        seleniumCapabilities.setCapability("sauce:options", sauceCapabilities);
        return seleniumCapabilities;
    }

    protected void addCapabilityIfDefined(MutableCapabilities capabilities, String capability) {
        Object value = getCapability(capability);
        if (value != null) {
            capabilities.setCapability(capability, value);
        }
    }

    public String getBuild() {
        if (build != null) {
            return build;
        } else if (getEnvironmentVariable(knownCITools.get("Jenkins")) != null) {
            return getEnvironmentVariable("BUILD_NAME") + ": " + getEnvironmentVariable("BUILD_NUMBER");
        } else if (getEnvironmentVariable(knownCITools.get("Bamboo")) != null) {
            return getEnvironmentVariable("bamboo_shortJobName") + ": " + getEnvironmentVariable("bamboo_buildNumber");
        } else if (getEnvironmentVariable(knownCITools.get("Travis")) != null) {
            return getEnvironmentVariable("TRAVIS_JOB_NAME") + ": " + getEnvironmentVariable("TRAVIS_JOB_NUMBER");
        } else if (getEnvironmentVariable(knownCITools.get("Circle")) != null) {
            return getEnvironmentVariable("CIRCLE_JOB") + ": " + getEnvironmentVariable("CIRCLE_BUILD_NUM");
        } else if (getEnvironmentVariable(knownCITools.get("GitLab")) != null) {
            return getEnvironmentVariable("CI_JOB_NAME") + ": " + getEnvironmentVariable("CI_JOB_ID");
        } else if (getEnvironmentVariable(knownCITools.get("TeamCity")) != null) {
            return getEnvironmentVariable("TEAMCITY_PROJECT_NAME") + ": " + getEnvironmentVariable("BUILD_NUMBER");
        } else {
            return "Build Time: " + System.currentTimeMillis();
        }
    }

    public boolean isKnownCI() {
        return !knownCITools.keySet().stream().allMatch((key) -> getEnvironmentVariable(key) == null);
    }

    // Use Case is pulling serialized information from JSON/YAML, converting it to a HashMap and passing it in
    // This is a preferred pattern as it avoids conditionals in code
    public void mergeCapabilities(Map<String, Object> capabilities) {
        capabilities.forEach(this::setCapability);
    }

    // This might be made public in future version; For now, no good reason to prefer it over direct accessor
    protected Object getCapability(String capability) {
        try {
            String getter = "get" + capability.substring(0, 1).toUpperCase() + capability.substring(1);
            Method declaredMethod = null;
            declaredMethod = this.getClass().getMethod(getter);
            return declaredMethod.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    // This might be made public in future version; For now, no good reason to prefer it over direct accessor
    protected void setCapability(String key, Object value) {
        if (primaryEnum.contains(key) && value.getClass().equals(String.class)) {
            setEnumCapability(key, (String) value);
        } else if (secondaryEnum.contains(key) && isKeyString((HashMap) value)) {
            setEnumCapability(key, (HashMap) value);
        } else {
            try {
                Class<?> type = SauceOptions.class.getDeclaredField(key).getType();
                String setter = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
                Method method = SauceOptions.class.getDeclaredMethod(setter, type);
                method.invoke(this, value);
            } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isKeyString(HashMap map) {
        return map.keySet().toArray()[0].getClass().equals(String.class);
    }

    private void setEnumCapability(String key, HashMap value) {
        if ("prerun".equals(key)) {
            Map<Prerun, Object> prerunMap = new HashMap<>();
            value.forEach((oldKey, val) -> {
                String keyString = Prerun.fromString((String) oldKey);
                prerunMap.put(Prerun.valueOf(keyString), val);
            });
            setPrerun(prerunMap);
        } else if ("timeouts".equals(key)) {
            Map<Timeouts, Integer> timeoutsMap = new HashMap<>();
            value.forEach((oldKey, val) -> {
                String keyString = Timeouts.fromString((String) oldKey);
                timeoutsMap.put(Timeouts.valueOf(keyString), (Integer) val);
            });
            setTimeouts(timeoutsMap);
        }
    }

    private void setEnumCapability(String key, String value) {
        switch (key) {
            case "browserName":
                if (!Browser.keys().contains(value)) {
                    String message = value + " is not a valid Browser, please choose from: " + Browser.keys();
                    throw new InvalidSauceOptionsArgumentException(message);
                } else {
                    setBrowserName(Browser.valueOf(Browser.fromString(value)));
                }
                break;
            case "platformName":
                if (!SaucePlatform.keys().contains(value)) {
                    String message = value + " is not a valid Platform, please choose from: " + SaucePlatform.keys();
                    throw new InvalidSauceOptionsArgumentException(message);
                } else {
                    setPlatformName(SaucePlatform.valueOf(SaucePlatform.fromString(value)));
                }
                break;
            case "jobVisibility":
                if (!JobVisibility.keys().contains(value)) {
                    String message = value + " is not a valid Job Visibility, please choose from: " + JobVisibility.keys();
                    throw new InvalidSauceOptionsArgumentException(message);
                } else {
                    setJobVisibility(JobVisibility.valueOf(JobVisibility.fromString(value)));
                }
                break;
            case "pageLoadStrategy":
                if (!PageLoadStrategy.keys().contains(value)) {
                    String message = value + " is not a valid Job Visibility, please choose from: " + PageLoadStrategy.keys();
                    throw new InvalidSauceOptionsArgumentException(message);
                } else {
                    setPageLoadStrategy(PageLoadStrategy.valueOf(PageLoadStrategy.fromString(value)));
                }
                break;
            case "unhandledPromptBehavior":
                if (!UnhandledPromptBehavior.keys().contains(value)) {
                    String message = value + " is not a valid Job Visibility, please choose from: " + UnhandledPromptBehavior.keys();
                    throw new InvalidSauceOptionsArgumentException(message);
                } else {
                    setUnhandledPromptBehavior(UnhandledPromptBehavior.valueOf(UnhandledPromptBehavior.fromString(value)));
                }
                break;
            default:
                break;
        }
    }

    protected void addAuthentication(MutableCapabilities caps) {
        caps.setCapability("username", getSauceUsername());
        caps.setCapability("accessKey", getSauceAccessKey());
    }

    protected String getSauceUsername() {
        if (getSystemProperty("SAUCE_USERNAME") != null) {
            return getSystemProperty("SAUCE_USERNAME");
        } else if (getEnvironmentVariable("SAUCE_USERNAME") != null) {
            return getEnvironmentVariable("SAUCE_USERNAME");
        } else {
            throw new SauceEnvironmentVariablesNotSetException("Sauce Username was not provided");
        }
    }

    protected String getSauceAccessKey() {
        if (getSystemProperty("SAUCE_ACCESS_KEY") != null) {
            return getSystemProperty("SAUCE_ACCESS_KEY");
        } else if (getEnvironmentVariable("SAUCE_ACCESS_KEY") != null) {
            return getEnvironmentVariable("SAUCE_ACCESS_KEY");
        } else {
            throw new SauceEnvironmentVariablesNotSetException("Sauce Access Key was not provided");
        }
    }

    protected String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    protected String getEnvironmentVariable(String key) {
        return System.getenv(key);
    }
}
