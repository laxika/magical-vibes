package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlockOnlyFlyersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "74")
public class CloudElemental extends Card {

    public CloudElemental() {
        addEffect(EffectSlot.STATIC, new BlockOnlyFlyersEffect());
    }
}
