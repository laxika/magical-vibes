package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "218")
public class SpikeTiller extends Card {

    public SpikeTiller() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        // {2}, Remove a +1/+1 counter from this creature: Put a +1/+1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)
                ),
                "{2}, Remove a +1/+1 counter from this creature: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));

        // {2}, Remove a +1/+1 counter from this creature: Target land becomes a 2/2 creature
        // that's still a land. Put a +1/+1 counter on it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new AnimatePermanentsEffect(
                                2, 2,
                                List.of(),
                                Set.of(),
                                null,
                                Set.of(),
                                GrantScope.TARGET,
                                EffectDuration.PERMANENT
                        ),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)
                ),
                "{2}, Remove a +1/+1 counter from this creature: Target land becomes a 2/2 creature that's still a land. Put a +1/+1 counter on it.",
                TargetFilters.land()
        ));
    }
}
