package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "139")
public class HavengulVampire extends Card {

    public HavengulVampire() {
        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        // Whenever another creature dies, put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
