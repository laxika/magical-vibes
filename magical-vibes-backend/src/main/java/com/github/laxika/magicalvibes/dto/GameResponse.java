package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GameResponse {

    private Long id;
    private String gameName;
    private String createdByUsername;
    private GameStatus status;
    private LocalDateTime createdAt;
    private int playerCount;
    private List<String> playerNames;
    private List<String> gameLog;
    private String startingPlayerName;
}
