package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "POR", collectorNumber = "84")
public class ChargingBandits extends Card {

    public ChargingBandits() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(2, 0));
    }
}
