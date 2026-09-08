package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackUntilNextTurnEffect;

@CardRegistration(set = "RNA", collectorNumber = "9")
public class ForbiddingSpirit extends Card {

    public ForbiddingSpirit() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RequirePaymentToAttackUntilNextTurnEffect(2));
    }
}
