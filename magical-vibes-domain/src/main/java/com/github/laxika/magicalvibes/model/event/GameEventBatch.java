package com.github.laxika.magicalvibes.model.event;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Ordered events committed by one outermost game mutation.
 */
public record GameEventBatch(
        UUID gameId,
        long causalActionId,
        long stateVersion,
        DispatchMode dispatchMode,
        List<GameEventEnvelope> events
) {

    public GameEventBatch {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(dispatchMode, "dispatchMode");
        events = List.copyOf(Objects.requireNonNull(events, "events"));
        if (causalActionId < 1) {
            throw new IllegalArgumentException("causalActionId must be positive");
        }
        if (stateVersion < 1) {
            throw new IllegalArgumentException("stateVersion must be positive");
        }
        for (GameEventEnvelope event : events) {
            if (!event.gameId().equals(gameId)
                    || event.causalActionId() != causalActionId
                    || event.stateVersion() != stateVersion) {
                throw new IllegalArgumentException("All batch events must share batch metadata");
            }
        }
    }

    public enum DispatchMode {
        LIVE,
        SUPPRESSED_SIMULATION
    }
}
