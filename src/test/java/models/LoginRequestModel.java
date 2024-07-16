package models;

import lombok.Data;

@Data
public class LoginRequestModel {
    String userName;
    String password;
}
