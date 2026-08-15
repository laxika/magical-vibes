package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MRD", collectorNumber = "160")
public class CullingScales extends Card {

    public CullingScales() {
        PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate lowestManaValue =
                new PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate();
        target(new PermanentPredicateTargetFilter(
                lowestManaValue,
                "Target must be a nonland permanent with the lowest mana value."
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new TargetPermanentMatches(lowestManaValue),
                new DestroyTargetPermanentEffect(), false));
    }
}
