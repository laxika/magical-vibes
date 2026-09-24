package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Exiles cards from library bottoms and tracks them with the source permanent. */
public record ExileBottomCardsToSourceEffect(DynamicAmount count, boolean faceDown,
                                             boolean eachOpponent) implements CardEffect {

    /** Exiles cards from the controller's library bottom face up. */
    public ExileBottomCardsToSourceEffect(DynamicAmount count) {
        this(count, false, false);
    }
}
