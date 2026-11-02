package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "27")
@CardRegistration(set = "S99", collectorNumber = "20")
@CardRegistration(set = "DDF", collectorNumber = "5")
@CardRegistration(set = "A25", collectorNumber = "22")
public class LoyalSentry extends Card {

    public LoyalSentry() {
        addTrigger(Triggers.blocks(), new DestroyBlockedCreatureAndSelfEffect());
    }
}
