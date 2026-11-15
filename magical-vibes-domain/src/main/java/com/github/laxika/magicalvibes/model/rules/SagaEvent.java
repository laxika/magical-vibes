package com.github.laxika.magicalvibes.model.rules;

import java.util.Objects;
import java.util.UUID;

/**
 * The final chapter ability of a Saga (CR 715.2e) triggered or resolved: the events "whenever the final chapter
 * ability of a Saga you control triggers" and "… resolves" abilities trigger on (CR 603.2).
 *
 * @param kind             whether the final chapter ability triggered or resolved
 * @param sagaControllerId the player who controls the Saga
 * @param sagaManaValue    the Saga's mana value
 */
public record SagaEvent(Kind kind, UUID sagaControllerId, int sagaManaValue) implements RulesEvent {

    public SagaEvent {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(sagaControllerId, "sagaControllerId");
    }

    @Override
    public SagaEvent deepCopy() {
        return this;
    }

    /** What happened to the final chapter ability. */
    public enum Kind {
        /** It triggered: it is put on the stack, before its targets are chosen. */
        FINAL_CHAPTER_TRIGGERED,
        /** It finished resolving. */
        FINAL_CHAPTER_RESOLVED
    }
}
