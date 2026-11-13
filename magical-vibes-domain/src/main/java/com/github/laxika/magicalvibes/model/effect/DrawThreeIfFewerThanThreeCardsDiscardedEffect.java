package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Draws three cards when fewer than three cards were discarded by the tracked discard flow: "Then if fewer than three
 * cards were discarded this way, you draw three cards" is part of the same resolution (CR 608.2c), never an ability of
 * its own.
 */
public record DrawThreeIfFewerThanThreeCardsDiscardedEffect()
        implements CardDrawingEffect, InlineDiscardFollowUpEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(3);
    }
}
