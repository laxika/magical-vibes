package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "148")
public class SlumberingDragon extends Card {

    public SlumberingDragon() {
        // This creature can't attack or block unless it has five or more +1/+1 counters on it.
        addStaticEffect(new CantAttackOrBlockUnlessEffect(
                new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE),
                "it has five or more +1/+1 counters on it"));

        // Whenever a creature attacks you or a planeswalker you control, put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
