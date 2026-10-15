package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SOK", collectorNumber = "116")
public class SpiralingEmbers extends Card {

    public SpiralingEmbers() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetEffect(new CardsInHand(CountScope.CONTROLLER)));
    }
}
