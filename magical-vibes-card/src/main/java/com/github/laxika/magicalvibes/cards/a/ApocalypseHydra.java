package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "98")
public class ApocalypseHydra extends Card {

    public ApocalypseHydra() {
        // This creature enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        // If X is 5 or more, it enters with an additional X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new SpellXAtLeast(5),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())));

        // {1}{R}, Remove a +1/+1 counter from this creature: It deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "{1}{R}, Remove a +1/+1 counter from Apocalypse Hydra: It deals 1 damage to any target."
        ));
    }
}
