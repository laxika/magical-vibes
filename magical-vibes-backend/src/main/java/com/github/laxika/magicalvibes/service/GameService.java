package com.github.laxika.magicalvibes.service;

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
    public GameResponse createGame(String gameName, Long userId) {
        log.info("Creating game '{}' for user ID: {}", gameName, userId);

        Game game = new Game(gameName, userId, "WAITING");
        game = gameRepository.save(game);

        GamePlayer gamePlayer = new GamePlayer(game.getId(), userId);
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

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (gamePlayerRepository.existsByGameIdAndUserId(gameId, userId)) {
            throw new IllegalStateException("You are already in this game");
        }

        if (!"WAITING".equals(game.getStatus())) {
            throw new IllegalStateException("Game is not accepting players");
        }

        GamePlayer gamePlayer = new GamePlayer(gameId, userId);
        gamePlayerRepository.save(gamePlayer);

        log.info("User {} successfully joined game {}", userId, gameId);
        return GameResponse.fromEntity(gameRepository.findById(gameId).orElseThrow());
    }
}
