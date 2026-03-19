package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "4")
public class BairdStewardOfArgive extends Card {

    public BairdStewardOfArgive() {
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(1));
    }
}
