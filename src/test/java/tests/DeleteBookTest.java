package tests;

import com.codeborne.selenide.Selenide;
import models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Arrays;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.TestSpec.requestSpec;
import static specs.TestSpec.responseSpec;

@Tag("api")
public class DeleteBookTest extends TestBase {

    @DisplayName("Проверка удаления книги из профиля")
    @Test
    void deleteBookTest() {

        LoginRequestModel loginBody = new LoginRequestModel();
        loginBody.setUserName("Yana");
        loginBody.setPassword("YanaAvvakumova1!");
        LoginResponseModel response = step("Отправляем запрос на авторизацию пользователя", () -> {
            return given(requestSpec)
                    .when()
                    .body(loginBody)
                    .post("/Account/v1/Login")
                    .then()
                    .spec(responseSpec)
                    .statusCode(200)
                    .extract().as(LoginResponseModel.class);
        });

        step("Проверяем ответ на авторизацию пользователя", () -> {
            assertThat(response.getToken()).isNotNull();
            assertThat(response.getUserId()).isNotNull();
            assertThat(response.getExpires()).isNotNull();
        });

        step("Отправляем запрос на удаление всех книг пользователя", () -> {
            given(requestSpec)
                    .when()
                    .header("Authorization", "Bearer " + response.getToken())
                    .body(loginBody)
                    .delete("/BookStore/v1/Books?UserId=" + response.getUserId())
                    .then()
                    .spec(responseSpec)
                    .statusCode(204);
        });

        IsbnModel book = new IsbnModel();
        book.setIsbn("9781449325862");
        AddBookRequestModel addBookBody = new AddBookRequestModel();
        addBookBody.setUserId(response.getUserId());
        addBookBody.setCollectionOfIsbns(Arrays.asList(book));
        AddBookResponseModel addBookResponse = step("Отправляем запрос на добавление книги в профиль пользователя", () -> {
            return given(requestSpec)
                    .when()
                    .header("Authorization", "Bearer " + response.getToken())
                    .body(addBookBody)
                    .post("/BookStore/v1/Books")
                    .then()
                    .spec(responseSpec)
                    .statusCode(201)
                    .extract().as(AddBookResponseModel.class);
        });

        step("Проверяем ответ на добавление книги в профиль пользователя", () -> {
            assertThat(addBookResponse.getBooks().size()).isEqualTo(1);
            assertThat(addBookResponse.getBooks().get(0).getIsbn()).isEqualTo(book.getIsbn());
        });

        step("Открываем страницу профиля", () -> {
            open("/images/Toolsqa.jpg");
            getWebDriver().manage().addCookie(new Cookie("userID", response.getUserId()));
            getWebDriver().manage().addCookie(new Cookie("token", response.getToken()));
            getWebDriver().manage().addCookie(new Cookie("expires", response.getExpires()));
            open("/profile");
        });

        step("Проверяем, что пользователь авторизован", () -> {
            $("#userName-value").shouldBe(visible).shouldHave(text(loginBody.getUserName()));
        });

        step("Проверяем, что в профиле пользователя отображается добавленная книга", () -> {
            $("[href=\"/profile?book=9781449325862\"]").shouldBe(visible).shouldHave(text("Git Pocket Guide"));
        });

        step("Удаляем в профиле пользователя книгу", () -> {
            $("#delete-record-undefined").click();
            $("#closeSmallModal-ok").click();
            Selenide.confirm();
        });

        step("Проверяем, что в профиле нет добавленных книг", () -> {
            $(".rt-noData").shouldBe(visible).shouldHave(text("No rows found"));
        });
    }
}
