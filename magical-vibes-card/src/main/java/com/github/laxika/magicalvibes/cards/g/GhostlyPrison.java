package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;

@CardRegistration(set = "CHK", collectorNumber = "10")
@CardRegistration(set = "PC2", collectorNumber = "7")
public class GhostlyPrison extends Card {

    public GhostlyPrison() {
        addEffect(EffectSlot.STATIC, RequirePaymentToAttackEffect.playerOnly(2));
    }
}
