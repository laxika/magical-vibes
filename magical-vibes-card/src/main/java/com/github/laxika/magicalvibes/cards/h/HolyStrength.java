package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "22")
public class HolyStrength extends Card {

    public HolyStrength() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(1, 2));
    }
}
