package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;

@CardRegistration(set = "IKO", collectorNumber = "217")
public class ZenithFlare extends Card {

    public ZenithFlare() {
        CardsInGraveyard cyclingCardsInGraveyard =
                new CardsInGraveyard(new CardHasCyclingPredicate(), CountScope.CONTROLLER);
        addSpellEffect(DealDamageEffect.toAnyTarget(cyclingCardsInGraveyard));
        addSpellEffect(new GainLifeEffect(cyclingCardsInGraveyard));
    }
}
