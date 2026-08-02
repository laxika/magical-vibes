package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "TMP", collectorNumber = "80")
public class Propaganda extends Card {

    public Propaganda() {
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(2));
    }
}
