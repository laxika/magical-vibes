package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Static replacement capability that skips an extra turn as it would begin while the source is
 * on the battlefield.
 */
public interface ExtraTurnSkipReplacementEffect extends CardEffect {

    /**
     * Returns whether this replacement applies to an extra turn for the given player.
     *
     * @param sourceControllerId controller of the permanent carrying this effect
     * @param extraTurnPlayerId player whose extra turn would begin
     */
    default boolean appliesTo(UUID sourceControllerId, UUID extraTurnPlayerId) {
        return true;
    }
}
