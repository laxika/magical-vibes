package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "213")
public class TrigonOfCorruption extends Card {

    public TrigonOfCorruption() {
        // Trigon of Corruption enters the battlefield with three charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));

        // {B}{B}, {T}: Put a charge counter on Trigon of Corruption.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}",
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{B}{B}, {T}: Put a charge counter on Trigon of Corruption."
        ));

        // {2}, {T}, Remove a charge counter from Trigon of Corruption: Put a -1/-1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE)
                ),
                "{2}, {T}, Remove a charge counter from Trigon of Corruption: Put a -1/-1 counter on target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
