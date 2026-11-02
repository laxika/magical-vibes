package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "24")
public class WarriorAngel extends Card {

    public WarriorAngel() {
        // Whenever this creature deals damage, you gain that much life.
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
