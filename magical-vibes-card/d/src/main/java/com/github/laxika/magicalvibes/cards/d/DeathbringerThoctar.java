package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "36")
@CardRegistration(set = "C13", collectorNumber = "184")
public class DeathbringerThoctar extends Card {

    public DeathbringerThoctar() {
        // Whenever another creature dies, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new MayEffect(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on this creature?"));

        // Remove a +1/+1 counter from this creature: It deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        DealDamageEffect.toAnyTarget(1)
                ),
                "Remove a +1/+1 counter from Deathbringer Thoctar: It deals 1 damage to any target."
        ));
    }
}
