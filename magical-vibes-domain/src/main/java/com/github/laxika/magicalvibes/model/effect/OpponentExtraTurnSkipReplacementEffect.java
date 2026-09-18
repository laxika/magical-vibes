package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Static replacement capability that skips extra turns taken by opponents of the source controller. */
public record OpponentExtraTurnSkipReplacementEffect() implements ExtraTurnSkipReplacementEffect {

    @Override
    public boolean appliesToExtraTurn(UUID sourceControllerId, UUID extraTurnPlayerId) {
        return !sourceControllerId.equals(extraTurnPlayerId);
    }
}
