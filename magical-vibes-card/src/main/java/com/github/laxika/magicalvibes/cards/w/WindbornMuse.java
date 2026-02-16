package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;

public class WindbornMuse extends Card {

    public WindbornMuse() {
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(2));
    }
}
