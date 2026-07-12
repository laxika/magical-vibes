package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;

@CardRegistration(set = "SHM", collectorNumber = "53")
public class ThoughtReflection extends Card {

    public ThoughtReflection() {
        addEffect(EffectSlot.STATIC, new DoubleDrawReplacementEffect());
    }
}
