package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/** Mills the controller's library, then may put any number of milled lands onto the battlefield tapped. */
public record MillControllerAndMayPutMilledLandsOntoBattlefieldEffect(DynamicAmount count)
        implements CardEffect {


    @Override
    public boolean referencesEventValue() {
        return count instanceof EventValue;
    }
}
