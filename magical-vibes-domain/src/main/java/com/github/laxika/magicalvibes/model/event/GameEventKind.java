package com.github.laxika.magicalvibes.model.event;

/**
 * Stable, transport-independent families of facts emitted by a completed game mutation.
 *
 * <p>Card-specific effects and low-level field changes deliberately do not get their own event
 * kinds. They invalidate observable state, record a decision, reveal private information, or end
 * the game.
 */
public enum GameEventKind {
    STATE_INVALIDATED,
    DECISION_REQUESTED,
    PRIVATE_REVEAL,
    GAME_ENDED
}
