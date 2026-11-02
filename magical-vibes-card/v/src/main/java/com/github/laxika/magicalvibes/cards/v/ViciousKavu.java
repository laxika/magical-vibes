package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "284")
public class ViciousKavu extends Card {

    public ViciousKavu() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
    }
}
