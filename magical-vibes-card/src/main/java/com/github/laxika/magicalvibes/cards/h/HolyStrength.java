package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;

public class HolyStrength extends Card {

    public HolyStrength() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureEffect(1, 2));
    }
}
