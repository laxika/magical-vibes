package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "134")
public class PheresBandTromper extends Card {

    public PheresBandTromper() {
        addTrigger(Triggers.becomesUntapped(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
