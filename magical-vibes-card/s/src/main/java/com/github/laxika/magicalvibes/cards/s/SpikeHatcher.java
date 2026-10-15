package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "126")
@CardRegistration(set = "TPR", collectorNumber = "197")
public class SpikeHatcher extends Card {

    public SpikeHatcher() {
        // This creature enters with six +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(6)));

        // {2}, Remove a +1/+1 counter from this creature: Put a +1/+1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "{2}, Remove a +1/+1 counter from Spike Hatcher: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));

        // {1}, Remove a +1/+1 counter from this creature: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new RegenerateEffect()),
                "{1}, Remove a +1/+1 counter from Spike Hatcher: Regenerate Spike Hatcher."
        ));
    }
}
