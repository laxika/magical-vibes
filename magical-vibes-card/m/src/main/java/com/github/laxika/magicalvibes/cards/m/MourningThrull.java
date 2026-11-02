package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "146")
@CardRegistration(set = "RVR", collectorNumber = "204")
public class MourningThrull extends Card {

    public MourningThrull() {
        // Whenever this creature deals damage, you gain that much life.
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
