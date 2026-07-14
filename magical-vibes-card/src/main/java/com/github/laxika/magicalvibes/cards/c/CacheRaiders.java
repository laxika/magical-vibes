package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "EVE", collectorNumber = "18")
public class CacheRaiders extends Card {

    public CacheRaiders() {
        // At the beginning of your upkeep, return a permanent you control to its owner's hand.
        // Mandatory choice among your own permanents (chosen at trigger time via the upkeep
        // permanent-target pipeline); there is always at least this creature to return.
        target(new PermanentPredicateTargetFilter(
                new PermanentControlledBySourceControllerPredicate(),
                "Target must be a permanent you control"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, ReturnToHandEffect.target());
    }
}
