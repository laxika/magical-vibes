package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "RTR", collectorNumber = "151")
public class CommonBond extends Card {

    public CommonBond() {
        // Put a +1/+1 counter on target creature. Put a +1/+1 counter on target creature.
        // Equivalent to distributing two +1/+1 counters among one or two target creatures.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ), 1, 2).addEffect(EffectSlot.SPELL,
                DistributeCountersAmongTargetsEffect.evenlyAmongTargets(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
