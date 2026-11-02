package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "75")
@CardRegistration(set = "TSR", collectorNumber = "125")
public class MirriTheCursed extends Card {

    public MirriTheCursed() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
