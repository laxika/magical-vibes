package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "173")
public class BalduvianHydra extends Card {

    public BalduvianHydra() {
        // This creature enters with X +1/+0 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ZERO, new XValue()));

        // Remove a +1/+0 counter from this creature: Prevent the next 1 damage that would be
        // dealt to it this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ZERO),
                        PreventDamageEffect.nextToSelf(1)
                ),
                "Remove a +1/+0 counter from this creature: Prevent the next 1 damage that would be dealt to it this turn."
        ));

        // {R}{R}{R}: Put a +1/+0 counter on this creature. Activate only during your upkeep.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}{R}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ZERO)),
                "{R}{R}{R}: Put a +1/+0 counter on this creature. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
