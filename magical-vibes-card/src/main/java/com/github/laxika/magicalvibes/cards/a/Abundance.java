package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;

@CardRegistration(set = "10E", collectorNumber = "249")
public class Abundance extends Card {

    public Abundance() {
        addEffect(EffectSlot.STATIC, new AbundanceDrawReplacementEffect());
    }
}
