package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Static replacement effect that skips an extra turn as it would begin while its source remains
 * on the battlefield.
 */
public record SkipExtraTurnReplacementEffect() implements ExtraTurnSkipReplacementEffect {

    @Override
    public boolean appliesToExtraTurn(UUID sourceControllerId, UUID extraTurnPlayerId) {
        return !sourceControllerId.equals(extraTurnPlayerId);
    }
}
