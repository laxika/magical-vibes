package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect;

@CardRegistration(set = "STH", collectorNumber = "20")
public class Temper extends Card {

    public Temper() {
        addEffect(EffectSlot.SPELL,
                new PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect(new XValue()));
    }
}
