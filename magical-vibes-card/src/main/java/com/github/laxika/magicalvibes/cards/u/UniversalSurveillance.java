package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "NEC", collectorNumber = "17")
@CardRegistration(set = "NEC", collectorNumber = "54")
@CardRegistration(set = "EOC", collectorNumber = "81")
public class UniversalSurveillance extends Card {

    public UniversalSurveillance() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
    }
}
