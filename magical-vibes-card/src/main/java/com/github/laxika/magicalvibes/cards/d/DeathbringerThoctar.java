package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "36")
public class DeathbringerThoctar extends Card {

    public DeathbringerThoctar() {
        // Whenever another creature dies, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                        "Put a +1/+1 counter on this creature?"));

        // Remove a +1/+1 counter from this creature: It deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "Remove a +1/+1 counter from Deathbringer Thoctar: It deals 1 damage to any target."
        ));
    }
}
