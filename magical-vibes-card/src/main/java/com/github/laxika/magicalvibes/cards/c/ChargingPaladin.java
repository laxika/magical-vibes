package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "POR", collectorNumber = "11")
@CardRegistration(set = "EXO", collectorNumber = "4")
@CardRegistration(set = "TPR", collectorNumber = "9")
@CardRegistration(set = "S99", collectorNumber = "12")
public class ChargingPaladin extends Card {

    public ChargingPaladin() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(0, 3));
    }
}
