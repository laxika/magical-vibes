package com.github.laxika.magicalvibes.controller;

import com.github.laxika.magicalvibes.dto.CreateGameRequest;
import com.github.laxika.magicalvibes.dto.GameResponse;
import com.github.laxika.magicalvibes.dto.JoinGameRequest;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping
    public ResponseEntity<GameResponse> createGame(@RequestBody CreateGameRequest request) {
        try {
            GameResponse game = gameService.createGame(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(game);
        } catch (Exception e) {
            log.error("Error creating game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> listGames() {
        try {
            List<GameResponse> games = gameService.listRunningGames();
            return ResponseEntity.ok(games);
        } catch (Exception e) {
            log.error("Error listing games", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameResponse> joinGame(@PathVariable Long gameId, @RequestBody JoinGameRequest request) {
        try {
            GameResponse game = gameService.joinGame(gameId, request.getUserId());
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            log.warn("Game not found: {}", gameId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Cannot join game: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error joining game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
