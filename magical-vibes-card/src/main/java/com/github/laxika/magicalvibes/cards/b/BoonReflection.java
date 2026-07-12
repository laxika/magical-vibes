package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;

@CardRegistration(set = "SHM", collectorNumber = "5")
public class BoonReflection extends Card {

    public BoonReflection() {
        addEffect(EffectSlot.STATIC, new DoubleLifeGainEffect());
    }
}
