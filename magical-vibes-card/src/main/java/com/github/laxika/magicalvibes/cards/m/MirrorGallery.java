package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleEffect;

@CardRegistration(set = "BOK", collectorNumber = "154")
public class MirrorGallery extends Card {

    public MirrorGallery() {
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleEffect());
    }
}
