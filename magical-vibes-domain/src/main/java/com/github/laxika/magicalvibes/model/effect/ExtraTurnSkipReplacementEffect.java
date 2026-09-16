package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Static replacement capability that skips an extra turn as it would begin while the source is
 * on the battlefield.
 */
public interface ExtraTurnSkipReplacementEffect extends CardEffect {

    /**
     * Returns whether this replacement applies to an extra turn taken by the given player.
     * Existing implementations apply to every player's extra turns by default.
     */
    default boolean appliesToExtraTurn(UUID sourceControllerId, UUID extraTurnPlayerId) {
        return true;
    }
}
