package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "118")
public class StromkirkPatrol extends Card {

    public StromkirkPatrol() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
