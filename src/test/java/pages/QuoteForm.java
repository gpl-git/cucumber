package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import support.TestContext;

import static support.TestContext.getConfig;

public class QuoteForm extends Page {

    public QuoteForm() {
        switch (TestContext.getConfig().getString("env")) {
            case "qa":
                url = "http://quote-qa.portnov.com/";
                break;
            case "staging":
                url = "http://quote-stage.portnov.com/";
                break;
            default:
                url = "https://skryabin.com/market/quote.html";
        }
    }

    @FindBy(name = "username")
    private WebElement username;

    @FindBy(name = "email")
    private WebElement email;

    @FindBy(id = "password")
    private WebElement password;

    @FindBy(id = "confirmPassword")
    private WebElement confirmPassword;

    @FindBy(id = "name")
    private WebElement name;

    // Dialog fields - begin
    @FindBy(name = "firstName")
    private WebElement firstName;

    @FindBy(name = "middleName")
    private WebElement middleName;

    @FindBy(name = "lastName")
    private WebElement lastName;

    @FindBy(xpath = "//span[text()='Save']")
    private WebElement saveButton;
    // Dialog fields - end

    @FindBy(name = "agreedToPrivacyPolicy")
    private WebElement privacy;

    @FindBy(id = "formSubmit")
    private WebElement submit;

    public void fillUsername(String text) {
        username.sendKeys(text);
    }

    public void fillEmail(String text) {
        email.sendKeys(text);
    }
    public void fillPassword(String text) {
        password.sendKeys(text);
    }

    public void fillConfirmPassword(String text) {
        confirmPassword.sendKeys(text);
    }

    public void fillName(String first, String last) {
        name.click();
        firstName.sendKeys(first);
        lastName.sendKeys(last);
        saveButton.click();
    }

    public void fillName(String first, String middle, String last) {
        name.click();
        firstName.sendKeys(first);
        middleName.sendKeys(middle);
        lastName.sendKeys(last);
        saveButton.click();
    }

    public void acceptPrivacy(){
        clickWithJS(privacy);
    }

    public void submitForm() {
        clickWithJS(submit);

    }










}
