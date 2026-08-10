package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "EXO", collectorNumber = "5")
public class Convalescence extends Card {

    public Convalescence() {
        // At the beginning of your upkeep, if you have 10 or less life, you gain 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControllerLifeAtMost(10), new GainLifeEffect(1)));
    }
}
