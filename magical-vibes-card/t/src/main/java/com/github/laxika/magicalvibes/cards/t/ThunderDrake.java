package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "73")
public class ThunderDrake extends Card {

    public ThunderDrake() {
        // Whenever you cast your second spell each turn, put a +1/+1 counter on this creature.
        addTrigger(Triggers.youCastSpell(), new NthSpellCastTriggerEffect(
                2,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))
        ));
    }
}
