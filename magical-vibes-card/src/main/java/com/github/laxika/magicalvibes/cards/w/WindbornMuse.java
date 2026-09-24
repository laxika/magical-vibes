package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "60")
@CardRegistration(set = "LGN", collectorNumber = "28")
@CardRegistration(set = "DMR", collectorNumber = "36")
@CardRegistration(set = "CMD", collectorNumber = "38")
public class WindbornMuse extends Card {

    public WindbornMuse() {
        addEffect(EffectSlot.STATIC, RequirePaymentToAttackEffect.playerOnly(2));
    }
}
