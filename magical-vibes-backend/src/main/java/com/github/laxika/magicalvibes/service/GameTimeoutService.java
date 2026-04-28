package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cleans up games whose players have disconnected.
 *
 * <p>Two timers per game:
 * <ul>
 *   <li><b>Both-disconnected</b> ({@code magicalvibes.game.timeout.both-disconnected}, default 5m) —
 *       when every human player has dropped, the game is removed (or, in tournaments, a random
 *       player is declared the winner so the bracket can advance).</li>
 *   <li><b>Single-disconnected</b> ({@code magicalvibes.game.timeout.single-disconnected},
 *       default 15m) — when one player has dropped but their opponent is still connected,
 *       the opponent is awarded the win.</li>
 * </ul>
 *
 * <p>Vs-AI games are closed immediately when the human leaves or disconnects — no timer.
 */
@Service
@Slf4j
public class GameTimeoutService {

    static final String AI_CONNECTION_PREFIX = "ai-";

    private final GameRegistry gameRegistry;
    private final GameOutcomeService gameOutcomeService;
    private final WebSocketSessionManager sessionManager;
    private final Duration bothDisconnectedTimeout;
    private final Duration singleDisconnectedTimeout;
    private final ScheduledExecutorService scheduler;
    private final Random random = new Random();

    private final Map<UUID, ScheduledFuture<?>> bothGoneTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> singleGoneTimers = new ConcurrentHashMap<>();

    public GameTimeoutService(GameRegistry gameRegistry,
                              @Lazy GameOutcomeService gameOutcomeService,
                              WebSocketSessionManager sessionManager,
                              @Value("${magicalvibes.game.timeout.both-disconnected:5m}") Duration bothDisconnectedTimeout,
                              @Value("${magicalvibes.game.timeout.single-disconnected:15m}") Duration singleDisconnectedTimeout) {
        this(gameRegistry, gameOutcomeService, sessionManager, bothDisconnectedTimeout, singleDisconnectedTimeout,
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "game-timeout-scheduler");
                    t.setDaemon(true);
                    return t;
                }));
    }

    GameTimeoutService(GameRegistry gameRegistry,
                       GameOutcomeService gameOutcomeService,
                       WebSocketSessionManager sessionManager,
                       Duration bothDisconnectedTimeout,
                       Duration singleDisconnectedTimeout,
                       ScheduledExecutorService scheduler) {
        this.gameRegistry = gameRegistry;
        this.gameOutcomeService = gameOutcomeService;
        this.sessionManager = sessionManager;
        this.bothDisconnectedTimeout = bothDisconnectedTimeout;
        this.singleDisconnectedTimeout = singleDisconnectedTimeout;
        this.scheduler = scheduler;
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    public void onPlayerDisconnect(UUID playerId) {
        GameData gameData = gameRegistry.getGameForPlayer(playerId);
        if (!isActiveGame(gameData)) {
            return;
        }

        synchronized (gameData) {
            if (!isActiveGame(gameData)) {
                return;
            }

            if (isVsAi(gameData)) {
                closeAiGame(gameData);
                return;
            }

            boolean anyOpponentConnected = false;
            for (UUID otherPlayerId : gameData.playerIds) {
                if (otherPlayerId.equals(playerId)) continue;
                if (isPlayerConnected(otherPlayerId)) {
                    anyOpponentConnected = true;
                    break;
                }
            }

            if (anyOpponentConnected) {
                armSingleGoneTimer(playerId, gameData.id);
            } else {
                for (UUID p : gameData.playerIds) {
                    cancel(singleGoneTimers.remove(p));
                }
                armBothGoneTimer(gameData.id);
            }
        }
    }

    public void onPlayerReconnect(UUID playerId) {
        cancel(singleGoneTimers.remove(playerId));

        GameData gameData = gameRegistry.getGameForPlayer(playerId);
        if (!isActiveGame(gameData)) {
            return;
        }

        synchronized (gameData) {
            if (!isActiveGame(gameData)) {
                return;
            }

            cancel(bothGoneTimers.remove(gameData.id));

            for (UUID otherPlayerId : gameData.playerIds) {
                if (otherPlayerId.equals(playerId)) continue;
                if (!isPlayerConnected(otherPlayerId) && !singleGoneTimers.containsKey(otherPlayerId)) {
                    armSingleGoneTimer(otherPlayerId, gameData.id);
                }
            }
        }
    }

    public void onGameFinished(GameData gameData) {
        cancel(bothGoneTimers.remove(gameData.id));
        for (UUID p : gameData.playerIds) {
            cancel(singleGoneTimers.remove(p));
        }
    }

    private void armSingleGoneTimer(UUID disconnectedPlayerId, UUID gameId) {
        cancel(singleGoneTimers.remove(disconnectedPlayerId));
        ScheduledFuture<?> future = scheduler.schedule(
                () -> singleGoneTimerFired(disconnectedPlayerId, gameId),
                singleDisconnectedTimeout.toMillis(), TimeUnit.MILLISECONDS);
        singleGoneTimers.put(disconnectedPlayerId, future);
        log.info("Armed {} single-disconnected timer for player {} in game {}",
                singleDisconnectedTimeout, disconnectedPlayerId, gameId);
    }

    private void armBothGoneTimer(UUID gameId) {
        cancel(bothGoneTimers.remove(gameId));
        ScheduledFuture<?> future = scheduler.schedule(
                () -> bothGoneTimerFired(gameId),
                bothDisconnectedTimeout.toMillis(), TimeUnit.MILLISECONDS);
        bothGoneTimers.put(gameId, future);
        log.info("Armed {} both-disconnected timer for game {}", bothDisconnectedTimeout, gameId);
    }

    private void singleGoneTimerFired(UUID disconnectedPlayerId, UUID gameId) {
        singleGoneTimers.remove(disconnectedPlayerId);
        GameData gameData = gameRegistry.get(gameId);
        if (!isActiveGame(gameData)) {
            return;
        }
        synchronized (gameData) {
            if (!isActiveGame(gameData)) {
                return;
            }
            if (isPlayerConnected(disconnectedPlayerId)) {
                return;
            }
            UUID opponentId = null;
            for (UUID p : gameData.playerIds) {
                if (!p.equals(disconnectedPlayerId)) {
                    opponentId = p;
                    break;
                }
            }
            if (opponentId == null || !isPlayerConnected(opponentId)) {
                return;
            }
            log.info("Single-disconnected timer fired: awarding game {} to {} (opponent {} did not reconnect)",
                    gameId, opponentId, disconnectedPlayerId);
            gameOutcomeService.declareWinner(gameData, opponentId);
        }
    }

    private void bothGoneTimerFired(UUID gameId) {
        bothGoneTimers.remove(gameId);
        GameData gameData = gameRegistry.get(gameId);
        if (!isActiveGame(gameData)) {
            return;
        }
        synchronized (gameData) {
            if (!isActiveGame(gameData)) {
                return;
            }
            for (UUID p : gameData.playerIds) {
                if (isPlayerConnected(p)) {
                    return;
                }
            }
            if (gameData.draftId != null) {
                List<UUID> playerIds = new ArrayList<>(gameData.playerIds);
                UUID winner = playerIds.get(random.nextInt(playerIds.size()));
                log.info("Both-disconnected timer fired for tournament game {}: declaring random winner {} so the draft can proceed",
                        gameId, winner);
                gameOutcomeService.declareWinner(gameData, winner);
            } else {
                log.info("Both-disconnected timer fired for casual game {}: removing without a winner", gameId);
                gameData.status = GameStatus.FINISHED;
                gameRegistry.remove(gameId);
            }
        }
    }

    private void closeAiGame(GameData gameData) {
        log.info("Closing vs-AI game {} because the human player left or disconnected", gameData.id);
        gameData.status = GameStatus.FINISHED;
        sessionManager.unregisterSession(AI_CONNECTION_PREFIX + gameData.id);
        gameRegistry.remove(gameData.id);
        cancel(bothGoneTimers.remove(gameData.id));
        for (UUID p : gameData.playerIds) {
            cancel(singleGoneTimers.remove(p));
        }
    }

    private boolean isActiveGame(GameData gameData) {
        if (gameData == null) return false;
        if (gameData.simulation) return false;
        GameStatus status = gameData.status;
        return status != GameStatus.WAITING && status != GameStatus.FINISHED;
    }

    public boolean isVsAi(GameData gameData) {
        for (UUID playerId : gameData.playerIds) {
            Connection conn = sessionManager.getConnectionByUserId(playerId);
            if (conn != null && conn.getId().startsWith(AI_CONNECTION_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerConnected(UUID playerId) {
        Connection conn = sessionManager.getConnectionByUserId(playerId);
        if (conn == null) return false;
        return sessionManager.isInGame(conn.getId());
    }

    private static void cancel(ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(false);
        }
    }
}
