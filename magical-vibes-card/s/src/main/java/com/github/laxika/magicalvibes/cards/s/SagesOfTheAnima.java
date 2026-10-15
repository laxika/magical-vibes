package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SagesOfTheAnimaDrawReplacementEffect;

@CardRegistration(set = "ARB", collectorNumber = "103")
public class SagesOfTheAnima extends Card {

    public SagesOfTheAnima() {
        addEffect(EffectSlot.STATIC, new SagesOfTheAnimaDrawReplacementEffect());
    }
}
