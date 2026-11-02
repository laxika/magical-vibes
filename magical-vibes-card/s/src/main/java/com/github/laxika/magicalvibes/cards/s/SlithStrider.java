package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "50")
@CardRegistration(set = "DDI", collectorNumber = "9")
public class SlithStrider extends Card {

    public SlithStrider() {
        addTrigger(Triggers.becomesBlocked(), new DrawCardEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
