package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.OpponentsControllingReturnedPermanents;

/** Draws one card for each opponent controlling a permanent returned earlier in this resolution. */
public record DrawCardForEachOpponentControllingReturnedPermanentEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new OpponentsControllingReturnedPermanents();
    }
}
