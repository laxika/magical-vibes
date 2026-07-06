package com.github.laxika.magicalvibes.model.amount;

/** Whose game objects a counting amount looks at, relative to the effect's controller. */
public enum CountScope {
    CONTROLLER,
    OPPONENTS,
    ANY_PLAYER,
    /**
     * The single player targeted by the effect (read from the stack entry's target channel via
     * {@code AmountContext.targetPermanentId} — which, for player-targeting effects, holds the
     * target player's id). Evaluates to nothing when there is no target.
     */
    TARGET_PLAYER
}
