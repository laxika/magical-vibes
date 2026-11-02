package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "84")
public class ChargingBandits extends Card {

    public ChargingBandits() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
    }
}
