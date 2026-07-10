package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "221")
public class IncrementalGrowth extends Card {

    public IncrementalGrowth() {
        // Put a +1/+1 counter on target creature, two +1/+1 counters on another
        // target creature, and three +1/+1 counters on a third target creature.
        // Each target group excludes already-chosen targets, enforcing distinctness.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        )).addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be a creature"
        )).addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Third target must be a creature"
        )).addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
    }
}
