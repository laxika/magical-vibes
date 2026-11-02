package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "226")
public class MoonriseCleric extends Card {

    public MoonriseCleric() {
        addTrigger(Triggers.attacks(), new GainLifeEffect(1));
    }
}
