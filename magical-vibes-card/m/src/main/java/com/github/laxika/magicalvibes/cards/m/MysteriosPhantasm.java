package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "38")
@CardRegistration(set = "OM1", collectorNumber = "39")
public class MysteriosPhantasm extends Card {

    public MysteriosPhantasm() {
        addTrigger(Triggers.attacks(), new MillEffect(1, MillRecipient.CONTROLLER));
    }
}
