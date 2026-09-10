package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SOS", collectorNumber = "159")
public class ShopkeepersBane extends Card {

    public ShopkeepersBane() {
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(2));
    }
}
