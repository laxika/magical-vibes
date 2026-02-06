package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.entity.Game;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameResponse {

    private Long id;
    private String gameName;
    private String createdByUsername;
    private String status;
    private LocalDateTime createdAt;
    private int playerCount;

    public GameResponse() {
    }

    public GameResponse(Long id, String gameName, String createdByUsername, String status, LocalDateTime createdAt, int playerCount) {
        this.id = id;
        this.gameName = gameName;
        this.createdByUsername = createdByUsername;
        this.status = status;
        this.createdAt = createdAt;
        this.playerCount = playerCount;
    }

    public static GameResponse fromEntity(Game game) {
        return new GameResponse(
                game.getId(),
                game.getGameName(),
                game.getCreatedBy() != null ? game.getCreatedBy().getUsername() : "Unknown",
                game.getStatus(),
                game.getCreatedAt(),
                game.getPlayers() != null ? game.getPlayers().size() : 0
        );
    }
}
