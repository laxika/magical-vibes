package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "BNG", collectorNumber = "22")
public class OreskosSunGuide extends Card {

    public OreskosSunGuide() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new GainLifeEffect(2));
    }
}
