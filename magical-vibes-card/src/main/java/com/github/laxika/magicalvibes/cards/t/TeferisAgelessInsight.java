package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawExceptFirstDrawStepDrawEffect;

@CardRegistration(set = "M21", collectorNumber = "76")
public class TeferisAgelessInsight extends Card {

    public TeferisAgelessInsight() {
        addEffect(EffectSlot.STATIC, new DoubleDrawExceptFirstDrawStepDrawEffect());
    }
}
