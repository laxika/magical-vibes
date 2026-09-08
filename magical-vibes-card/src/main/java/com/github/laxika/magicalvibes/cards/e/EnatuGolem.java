package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ROE", collectorNumber = "217")
public class EnatuGolem extends Card {

    public EnatuGolem() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(4));
    }
}
