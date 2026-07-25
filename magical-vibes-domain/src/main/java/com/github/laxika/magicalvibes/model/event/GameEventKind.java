package com.github.laxika.magicalvibes.model.event;

/**
 * Stable, transport-independent families of facts emitted by a completed game mutation.
 *
 * <p>Card-specific effects and low-level field changes deliberately do not get their own event
 * kinds. They invalidate observable state, record a decision, reveal private information, or end
 * the game.
 */
public enum GameEventKind {
    /**
     * Authoritative game state changed and recipient-specific views must be rebuilt after the
     * mutation completes.
     */
    STATE_INVALIDATED,

    /**
     * A player must answer a new or replayed decision independently of state refresh coalescing.
     */
    DECISION_REQUESTED,

    /**
     * Hidden card information was revealed to the event's explicit private audience.
     */
    PRIVATE_REVEAL,

    /**
     * A player's mulligan or keep action completed and must use the existing public notification.
     */
    MULLIGAN_RESOLVED,

    /**
     * The game reached a terminal win or draw result.
     */
    GAME_ENDED
}
