package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "166")
public class BloodResearcher extends Card {

    public BloodResearcher() {
        addTrigger(Triggers.youGainLife(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
