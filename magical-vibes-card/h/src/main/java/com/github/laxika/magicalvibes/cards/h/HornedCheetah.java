package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "251")
public class HornedCheetah extends Card {

    public HornedCheetah() {
        // Whenever this creature deals damage, you gain that much life.
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
