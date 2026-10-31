package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCreaturesEnterTappedEffect;

public class RadiantRestraints extends Card {

    public RadiantRestraints() {
        addStaticEffect(new EnchantedPlayerCreaturesEnterTappedEffect());
    }
}
