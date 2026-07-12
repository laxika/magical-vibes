package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;


@CardRegistration(set = "SHM", collectorNumber = "70")
public class IncrementalBlight extends Card {

    public IncrementalBlight() {
        // Put a -1/-1 counter on target creature, ...
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        )).addEffect(EffectSlot.SPELL,
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // ... two -1/-1 counters on another target creature, ...
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be another creature"
        )).addEffect(EffectSlot.SPELL,
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 2));

        // ... and three -1/-1 counters on a third target creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Third target must be another creature"
        )).addEffect(EffectSlot.SPELL,
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3));
    }
}
