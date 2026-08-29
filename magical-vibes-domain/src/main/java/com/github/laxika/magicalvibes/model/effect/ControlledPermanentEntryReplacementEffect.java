package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for a static effect that changes how matching permanents enter the battlefield.
 */
public interface ControlledPermanentEntryReplacementEffect extends CardEffect {

    PermanentPredicate enteringPermanentPredicate();

    int additionalCounterCount(Permanent enteringPermanent);
}
