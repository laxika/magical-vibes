package com.github.laxika.magicalvibes.model.event;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Ordered events committed by one outermost game mutation.
 */
public record GameEventBatch(
        UUID gameId,
        UUID causalActionId,
        long stateVersion,
        DispatchMode dispatchMode,
        List<GameEventEnvelope> events
) {

    public GameEventBatch {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(causalActionId, "causalActionId");
        Objects.requireNonNull(dispatchMode, "dispatchMode");
        events = List.copyOf(Objects.requireNonNull(events, "events"));
        if (stateVersion < 1) {
            throw new IllegalArgumentException("stateVersion must be positive");
        }
        for (GameEventEnvelope event : events) {
            if (!event.gameId().equals(gameId)
                    || !event.causalActionId().equals(causalActionId)
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
