package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Draws three cards when fewer than three cards were discarded by the tracked discard flow. */
public record DrawThreeIfFewerThanThreeCardsDiscardedEffect()
        implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(3);
    }
}
