package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MetalcraftKeywordEffect;

@CardRegistration(set = "SOM", collectorNumber = "114")
public class CarapaceForger extends Card {

    public CarapaceForger() {
        addEffect(EffectSlot.STATIC, new MetalcraftKeywordEffect(null, 2, 2));
    }
}
