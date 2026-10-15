package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MMQ", collectorNumber = "268")
public class SacredPrey extends Card {

    public SacredPrey() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new GainLifeEffect(1));
    }
}
