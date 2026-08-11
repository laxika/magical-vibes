package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "M20", collectorNumber = "91")
public class BloodthirstyAerialist extends Card {

    public BloodthirstyAerialist() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
