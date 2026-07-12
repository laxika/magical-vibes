package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "122")
public class ManaReflection extends Card {

    public ManaReflection() {
        addEffect(EffectSlot.STATIC, new ManaReflectionEffect());
    }
}
