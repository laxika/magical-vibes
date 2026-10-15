package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;

@CardRegistration(set = "IKO", collectorNumber = "217")
public class ZenithFlare extends Card {

    public ZenithFlare() {
        CardsInGraveyard cyclingCardsInGraveyard =
                new CardsInGraveyard(new CardHasCyclingPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(cyclingCardsInGraveyard));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(cyclingCardsInGraveyard));
    }
}
