package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "10")
public class DrannithHealer extends Card {

    public DrannithHealer() {
        addTrigger(Triggers.youCycleCard(),
                new GainLifeEffect(1));
        addCycling("{1}");
    }
}
