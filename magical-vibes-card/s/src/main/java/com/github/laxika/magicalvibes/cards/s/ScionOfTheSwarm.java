package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "121")
public class ScionOfTheSwarm extends Card {

    public ScionOfTheSwarm() {
        addTrigger(Triggers.youGainLife(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
