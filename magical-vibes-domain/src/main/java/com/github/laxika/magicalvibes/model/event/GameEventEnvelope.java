package com.github.laxika.magicalvibes.model.event;

import java.util.Objects;
import java.util.UUID;

/**
 * Sequenced metadata around one immutable domain fact.
 */
public record GameEventEnvelope(
        UUID gameId,
        long sequence,
        UUID causalActionId,
        long stateVersion,
        GameEventKind kind,
        GameEventFact fact,
        GameEventAudience audience
) {

    public GameEventEnvelope {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(causalActionId, "causalActionId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(fact, "fact");
        Objects.requireNonNull(audience, "audience");
        if (sequence < 1) {
            throw new IllegalArgumentException("sequence must be positive");
        }
        if (stateVersion < 1) {
            throw new IllegalArgumentException("stateVersion must be positive");
        }
        if (kind != fact.kind()) {
            throw new IllegalArgumentException("Envelope kind must match fact kind");
        }
    }
}
