package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceBuybackCostEffect;

@CardRegistration(set = "EXO", collectorNumber = "134")
public class MemoryCrystal extends Card {

    public MemoryCrystal() {
        addEffect(EffectSlot.STATIC, new ReduceBuybackCostEffect(2));
    }
}
