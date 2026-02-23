package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;

@CardRegistration(set = "10E", collectorNumber = "185")
public class UnholyStrength extends Card {

    public UnholyStrength() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(2, 1));
    }
}
