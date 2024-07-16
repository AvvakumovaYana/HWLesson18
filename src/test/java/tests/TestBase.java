package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import helpers.Attach;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.util.Map;


public class TestBase {
    @BeforeAll
    static void beforeAll() throws Exception {
        Configuration.baseUrl = "https://demoqa.com";
        Configuration.pageLoadStrategy = "eager";
        Configuration.timeout = 6000;

        SelenideLogger.addListener("allure", new AllureSelenide());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("selenoid:options", Map.<String, Object>of(
                "enableVNC", true,
                "enableVideo", true
        ));
        Configuration.browserCapabilities = capabilities;

        Configuration.remote = System.getProperty("Wdhost","https://user1:1234@selenoid.autotests.cloud/wd/hub");
        Configuration.browser = System.getProperty("Browser","chrome");
        if (Configuration.browser.equals("chrome")) {
            Configuration.browserVersion = System.getProperty("ChromeVersion", "122.0");
        }
        else if (Configuration.browser.equals("firefox")) {
            Configuration.browserVersion = System.getProperty("FirefoxVersion","123.0");
        }
        else {
            throw new Exception("Неверный браузер! " + Configuration.browser);
        }
        Configuration.browserSize = System.getProperty("BrowserSize","1920x1080");
    }

    @AfterEach
    void addAttachments() {
        Attach.screenshotAs("Last screenshot");
        Attach.pageSource();
        Attach.browserConsoleLogs();
        Attach.addVideo();
    }
}