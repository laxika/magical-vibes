package com.github.laxika.magicalvibes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinGameRequest {

    private Long userId;

    public JoinGameRequest() {
    }

    public JoinGameRequest(Long userId) {
        this.userId = userId;
    }
}
