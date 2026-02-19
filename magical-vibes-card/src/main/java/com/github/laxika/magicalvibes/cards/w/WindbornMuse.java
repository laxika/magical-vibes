package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "60")
public class WindbornMuse extends Card {

    public WindbornMuse() {
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(2));
    }
}
