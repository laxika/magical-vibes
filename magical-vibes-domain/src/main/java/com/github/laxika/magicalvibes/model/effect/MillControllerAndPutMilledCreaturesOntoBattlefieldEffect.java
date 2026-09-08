package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Mills cards from the controller's library, then puts up to {@code maxCount} creature cards
 * milled by this resolution onto the battlefield.
 */
public record MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(DynamicAmount count, int maxCount)
        implements CardEffect {

    public MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(int count, int maxCount) {
        this(new Fixed(count), maxCount);
    }

    public MillControllerAndPutMilledCreaturesOntoBattlefieldEffect {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount cannot be negative");
        }
    }

    @Override
    public boolean referencesEventValue() {
        return count instanceof EventValue;
    }
}
