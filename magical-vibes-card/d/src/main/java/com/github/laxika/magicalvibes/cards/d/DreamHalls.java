package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SharedColorDiscardAlternativeCostEffect;

@CardRegistration(set = "STH", collectorNumber = "28")
@CardRegistration(set = "TPR", collectorNumber = "46")
public class DreamHalls extends Card {

    public DreamHalls() {
        addEffect(EffectSlot.STATIC, new SharedColorDiscardAlternativeCostEffect());
    }
}
