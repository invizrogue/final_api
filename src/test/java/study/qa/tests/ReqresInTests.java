package study.qa.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import study.qa.model.*;
import study.qa.specs.Endpoints;

import static org.hamcrest.Matchers.*;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static study.qa.specs.ReqresSpec.*;

public class ReqresInTests {

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("RESOURCE")})
    @DisplayName("Проверка, что количество ресурсов на странице меньше или равно шести")
    void checkCountItemsPerPageInResourcesListTest() {
        ResourceListResponseModel response =
                step("Запрос на загрузку списка ресурсов", () ->

                        given(commonRequestSpec)
                                .when()
                                .get(Endpoints.getListResource)
                                .then()
                                .spec(commonResponseSpec)
                                .extract().as(ResourceListResponseModel.class));

        step("Проверка, что размер выводимого списка меньше или равен 6", () ->
                assertThat(response.getData()).hasSizeLessThanOrEqualTo(6));
    }

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("USER")})
    @DisplayName("Проверка успешности регистрации пользователя и выданный токен содержит строку" +
            "из букв или цифр длиной 17 символов")
    void checkSuccessfulRegisterTest() {
        RegisterUserRequestModel request = new RegisterUserRequestModel();
        request.setEmail("eve.holt@reqres.in");
        request.setPassword("pistol");

        RegisterUserResponseModel response =
                step("Запрос на регистрацию пользователя", () ->
                        given(commonRequestSpec)
                                .when()
                                .body(request)
                                .post(Endpoints.postRegister)
                                .then()
                                .spec(commonResponseSpec)
                                .extract().as(RegisterUserResponseModel.class));

        step("Проверка выданного токена", () ->
                assertThat(response.getToken()).matches("\\w{17}"));
    }

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("USER")})
    @DisplayName("Проверка невозможности регистрации несозданных раннее пользователей")
    void checkUndefinedUserRegisterTest() {
        RegisterUserRequestModel request = new RegisterUserRequestModel();
        request.setEmail("test@test.tt");
        request.setPassword("pa$$w0rd");

        RegisterUserResponseModel response =
                step("Запрос на регистрацию пользователя", () ->
                        given(commonRequestSpec)
                                .when()
                                .body(request)
                                .post(Endpoints.postRegister)
                                .then()
                                .spec(badrequestResponseSpec)
                                .extract().as(RegisterUserResponseModel.class));

        step("Проверка сообщения об ошибке", () ->
                assertThat(response.getError())
                        .isEqualTo("Note: Only defined users succeed registration"));

    }

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("USER")})
    @DisplayName("Проверка кода ответа 204 при удалении пользователя")
    void checkDeleteUserTest() {

        Integer status =
                step("Запрос на удаление существующего пользователя", () ->
                        given(commonRequestSpec)
                                .when()
                                .delete(Endpoints.deleteDeleteN + 3)
                                .then()
                                .spec(otherResponseSpec)
                                .extract().statusCode());

        step("Проверка, что код ответа равен 204", () ->
                assertThat(status).isEqualTo(204));
    }

    @ValueSource(ints = {1, 3, 6})
    @Tags({@Tag("BLOCKER"), @Tag("RESOURCE")})
    @ParameterizedTest(name = "Проверка, что id={0} в строке запроса равен id пользователя в теле ответа")
    void checkSingleResourceTest(int testId) {

        ResourceListDataResponseModel data =
                step("Запрос на получение данных о пользователе с id=" + testId, () ->
                        given(commonRequestSpec)
                                .when()
                                .get(Endpoints.getSingleResourceN + testId)
                                .then()
                                .spec(commonResponseSpec)
                                .extract().body().jsonPath().getObject("data", ResourceListDataResponseModel.class));

        step("Проверка что id=" + testId + " в строке запроса равен id пользователя в теле ответа", () ->
                assertThat(data.getId()).isEqualTo(testId));
    }

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("RESOURCE")})
    @DisplayName("Проверка отсутствия ресурса с id=0")
    void checkNotFoundResourceTest() {
        Integer status =
                step("Запрос на получение информации о ресурсе", () ->
                        given(commonRequestSpec)
                                .when()
                                .get(Endpoints.getSingleResourceN + 0)
                                .then()
                                .spec(otherResponseSpec)
                                .extract().statusCode());

        step("Проверка, что код ответа равен 404", () ->
                assertThat(status).isEqualTo(404));
    }

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("USER")})
    @DisplayName("Проверка успешности создания пользователя: статус 201, id содержит цифры")
    void checkSuccessfulCreateUserTest() {
        CreateUserRequestModel request = new CreateUserRequestModel();
        request.setName("morpheus");
        request.setJob("leader");

        Integer status =
                step("Запрос на создание пользователя", () ->
                        given(commonRequestSpec)
                                .when()
                                .body(request)
                                .post(Endpoints.postCreate)
                                .then()
                                .spec(otherResponseSpec)
                                .body("id", matchesRegex("^\\d+$"))
                                .extract().statusCode());

        step("Проверка, что код ответа равен 201", () ->
                assertThat(status).isEqualTo(201));
    }

    @Test
    @Tags({@Tag("BLOCKER"), @Tag("USER")})
    @DisplayName("Проверка успешности выполнения запроса на обновление информации о пользователе: " +
            "код статуса 201 и название должности в теле запроса равно названию должности в теле ответа")
    void checkSuccessfulPutUserTest() {

        String job = "zion resident";
        UpdateUserRequestModel request = new UpdateUserRequestModel();
        request.setName("morpheus");
        request.setJob(job);

        UpdateUserResponseModel response =
                step("Запрос на создание пользователя", () ->
                given(commonRequestSpec)
                        .when()
                        .body(request)
                        .post(Endpoints.putUpdateN + 2)
                        .then()
                        .spec(otherResponseSpec)
                        .statusCode(201)
                        .extract().body().jsonPath().getObject(".", UpdateUserResponseModel.class));

        step("Проверка что job=" + job + " в теле запроса равен job пользователя в теле ответа", () ->
                assertThat(response.getJob()).isEqualTo(job));
    }
}
