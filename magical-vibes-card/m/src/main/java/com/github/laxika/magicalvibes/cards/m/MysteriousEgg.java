package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "3")
public class MysteriousEgg extends Card {

    public MysteriousEgg() {
        addTrigger(Triggers.mutates(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
