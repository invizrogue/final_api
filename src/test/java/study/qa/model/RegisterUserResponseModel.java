package study.qa.model;

import lombok.Data;

@Data
public class RegisterUserResponseModel {

    Integer id;
    String token;
    String error;
}
