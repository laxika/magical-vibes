package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCreaturesEnterTappedEffect;

public class RadiantRestraints extends Card {

    public RadiantRestraints() {
        addEffect(EffectSlot.STATIC, new EnchantedPlayerCreaturesEnterTappedEffect());
    }
}
