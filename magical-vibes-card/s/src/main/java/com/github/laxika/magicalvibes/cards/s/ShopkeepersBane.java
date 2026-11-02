package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "159")
public class ShopkeepersBane extends Card {

    public ShopkeepersBane() {
        addTrigger(Triggers.attacks(), new GainLifeEffect(2));
    }
}
