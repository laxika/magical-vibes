package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Canonical owner of authoritative structured game-log mutation and event recording.
 */
@Service
@RequiredArgsConstructor
public class GameLogService {

    private final GameMutationCoordinator mutationCoordinator;

    /**
     * Appends one public structured entry in the current action and records both its immutable
     * diagnostic fact and the coalescible projection invalidation.
     */
    public void append(GameData gameData, GameLogEntry logEntry) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(logEntry, "logEntry");
        if (!mutationCoordinator.isInAction(gameData)) {
            throw new IllegalStateException(
                    "Game logs may only be appended inside their game's mutation scope");
        }

        int logIndex = gameData.gameLog.size();
        gameData.gameLog.add(logEntry);
        mutationCoordinator.emit(
                gameData,
                new GameEventFact.GameLogAppended(logIndex),
                GameEventAudience.allPlayers());
        mutationCoordinator.emit(
                gameData,
                new GameEventFact.StateInvalidated(GameEventFact.StateSection.GAME_LOG),
                GameEventAudience.allPlayers());
    }
}
