package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Mills cards from the controller's library, then puts up to {@code maxCount} creature cards
 * milled by this resolution onto the battlefield.
 *
 * @param returnSourceToHand when true, returns the source to its owner's hand in the same
 *                           graveyard-leave event as the selected creatures
 */
public record MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(DynamicAmount count, int maxCount,
                                                                     boolean returnSourceToHand)
        implements CardEffect {

    public MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(DynamicAmount count, int maxCount) {
        this(count, maxCount, false);
    }

    public MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(int count, int maxCount) {
        this(new Fixed(count), maxCount, false);
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
