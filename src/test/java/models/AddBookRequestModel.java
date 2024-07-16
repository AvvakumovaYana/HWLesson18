package models;

import lombok.Data;

import java.util.List;

@Data
public class AddBookRequestModel {
    String userId;
    List<IsbnModel> collectionOfIsbns;
}
