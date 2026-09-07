package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "BLB", collectorNumber = "226")
public class MoonriseCleric extends Card {

    public MoonriseCleric() {
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(1));
    }
}
