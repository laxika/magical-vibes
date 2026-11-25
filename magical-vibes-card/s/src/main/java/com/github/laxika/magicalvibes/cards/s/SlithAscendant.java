package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "23")
public class SlithAscendant extends Card {

    public SlithAscendant() {
        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
