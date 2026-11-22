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
     * diagnostic fact and the coalescible projection invalidation. The card of a face-down spell or permanent
     * is left out of it (CR 708.5).
     */
    public void append(GameData gameData, GameLogEntry logEntry) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(logEntry, "logEntry");
        record(gameData, FaceDownVisibility.redact(gameData, logEntry));
    }

    /**
     * {@link #append}, for an entry that reveals a card that is or becomes face down: an instant or sorcery card
     * that can't be turned face up (CR 701.40g), or a card put onto the battlefield face down from a public zone.
     */
    public void appendRevealing(GameData gameData, GameLogEntry logEntry) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(logEntry, "logEntry");
        record(gameData, logEntry);
    }

    private void record(GameData gameData, GameLogEntry logEntry) {
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
