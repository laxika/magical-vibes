package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AKH", collectorNumber = "109")
public class SplendidAgony extends Card {

    public SplendidAgony() {
        // Distribute two -1/-1 counters among one or two target creatures.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ), 1, 2).addEffect(EffectSlot.SPELL,
                new DistributeCountersAmongTargetsEffect(CounterType.MINUS_ONE_MINUS_ONE, 2));
    }
}
