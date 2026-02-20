package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceEffect;

@CardRegistration(set = "10E", collectorNumber = "176")
public class SengirVampire extends Card {

    public SengirVampire() {
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new PutPlusOnePlusOneCounterOnSourceEffect(1));
    }
}
