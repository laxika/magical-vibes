package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MIR", collectorNumber = "273")
public class PhyrexianPurge extends Card {

    public PhyrexianPurge() {
        // This spell costs 3 life more to cast for each target.
        // Destroy any number of target creatures.
        setAdditionalLifeCostPerTarget(3);
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), 0, 99).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
