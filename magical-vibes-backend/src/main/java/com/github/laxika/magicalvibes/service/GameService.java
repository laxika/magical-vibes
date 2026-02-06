package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.dto.CreateGameRequest;
import com.github.laxika.magicalvibes.dto.GameResponse;
import com.github.laxika.magicalvibes.entity.Game;
import com.github.laxika.magicalvibes.entity.GamePlayer;
import com.github.laxika.magicalvibes.repository.GamePlayerRepository;
import com.github.laxika.magicalvibes.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;

    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        log.info("Creating game '{}' for user ID: {}", request.getGameName(), request.getUserId());

        // Create the game
        Game game = new Game(request.getGameName(), request.getUserId(), "WAITING");
        game = gameRepository.save(game);

        // Add the creator as the first player
        GamePlayer gamePlayer = new GamePlayer(game.getId(), request.getUserId());
        gamePlayerRepository.save(gamePlayer);

        log.info("Game created with ID: {}", game.getId());
        return GameResponse.fromEntity(gameRepository.findById(game.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<GameResponse> listRunningGames() {
        log.debug("Listing all running games");
        List<Game> games = gameRepository.findByStatus("WAITING");
        return games.stream()
                .map(GameResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GameResponse joinGame(Long gameId, Long userId) {
        log.info("User {} attempting to join game {}", userId, gameId);

        // Check if game exists
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        // Check if user is already in the game
        if (gamePlayerRepository.existsByGameIdAndUserId(gameId, userId)) {
            log.warn("User {} is already in game {}", userId, gameId);
            throw new IllegalStateException("You are already in this game");
        }

        // Check if game is still accepting players
        if (!"WAITING".equals(game.getStatus())) {
            log.warn("Game {} is not accepting players (status: {})", gameId, game.getStatus());
            throw new IllegalStateException("Game is not accepting players");
        }

        // Add player to game
        GamePlayer gamePlayer = new GamePlayer(gameId, userId);
        gamePlayerRepository.save(gamePlayer);

        log.info("User {} successfully joined game {}", userId, gameId);
        return GameResponse.fromEntity(gameRepository.findById(gameId).orElseThrow());
    }
}
