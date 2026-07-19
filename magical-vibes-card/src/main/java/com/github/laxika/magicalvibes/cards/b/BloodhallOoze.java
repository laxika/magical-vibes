package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "59")
public class BloodhallOoze extends Card {

    public BloodhallOoze() {
        // At the beginning of your upkeep, if you control a black permanent, you may
        // put a +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Bloodhall Ooze?")));

        // At the beginning of your upkeep, if you control a green permanent, you may
        // put a +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentColorInPredicate(Set.of(CardColor.GREEN))),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Bloodhall Ooze?")));
    }
}
