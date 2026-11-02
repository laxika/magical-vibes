package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "107")
@CardRegistration(set = "DD2", collectorNumber = "39")
@CardRegistration(set = "JVC", collectorNumber = "39")
public class SlithFirewalker extends Card {

    public SlithFirewalker() {
        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
