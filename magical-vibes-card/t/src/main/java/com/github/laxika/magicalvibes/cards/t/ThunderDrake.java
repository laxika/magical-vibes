package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "73")
public class ThunderDrake extends Card {

    public ThunderDrake() {
        // Whenever you cast your second spell each turn, put a +1/+1 counter on this creature.
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
