package com.github.laxika.magicalvibes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GameResponse {

    private Long id;
    private String gameName;
    private String createdByUsername;
    private String status;
    private LocalDateTime createdAt;
    private int playerCount;
}
