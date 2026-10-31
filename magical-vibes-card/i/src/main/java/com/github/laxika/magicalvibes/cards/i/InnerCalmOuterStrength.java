package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOK", collectorNumber = "133")
public class InnerCalmOuterStrength extends Card {

    public InnerCalmOuterStrength() {
        // Target creature gets +X/+X until end of turn, where X is the number of cards in your hand.
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(handSize, handSize));
    }
}
