package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "SOK", collectorNumber = "116")
public class SpiralingEmbers extends Card {

    public SpiralingEmbers() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new CardsInHand(CountScope.CONTROLLER)));
    }
}
