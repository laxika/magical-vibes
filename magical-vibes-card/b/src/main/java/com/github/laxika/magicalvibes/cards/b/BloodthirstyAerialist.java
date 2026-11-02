package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "91")
@CardRegistration(set = "C21", collectorNumber = "136")
public class BloodthirstyAerialist extends Card {

    public BloodthirstyAerialist() {
        addTrigger(Triggers.youGainLife(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
