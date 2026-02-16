package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlockOnlyFlyersEffect;

public class CloudElemental extends Card {

    public CloudElemental() {
        addEffect(EffectSlot.STATIC, new BlockOnlyFlyersEffect());
    }
}
