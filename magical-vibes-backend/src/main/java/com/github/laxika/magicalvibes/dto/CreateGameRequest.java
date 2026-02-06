package com.github.laxika.magicalvibes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGameRequest {

    private String gameName;
    private Long userId;

    public CreateGameRequest() {
    }

    public CreateGameRequest(String gameName, Long userId) {
        this.gameName = gameName;
        this.userId = userId;
    }
}
