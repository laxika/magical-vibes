package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LandEquilibriumEffect;

@CardRegistration(set = "LEG", collectorNumber = "64")
public class LandEquilibrium extends Card {

    public LandEquilibrium() {
        addEffect(EffectSlot.STATIC, new LandEquilibriumEffect());
    }
}
