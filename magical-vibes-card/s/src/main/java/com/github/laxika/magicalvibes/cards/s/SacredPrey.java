package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "268")
public class SacredPrey extends Card {

    public SacredPrey() {
        addTrigger(Triggers.becomesBlocked(), new GainLifeEffect(1));
    }
}
