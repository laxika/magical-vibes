package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "22")
public class OreskosSunGuide extends Card {

    public OreskosSunGuide() {
        addTrigger(Triggers.becomesUntapped(), new GainLifeEffect(2));
    }
}
