package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "19")
public class JollyGerbils extends Card {

    public JollyGerbils() {
        addTrigger(Triggers.youGiveGift(), new DrawCardEffect());
    }
}
