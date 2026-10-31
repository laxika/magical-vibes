package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "THB", collectorNumber = "12")
@CardRegistration(set = "THB", collectorNumber = "299")
public class EidolonOfObstruction extends Card {

    public EidolonOfObstruction() {
        addStaticEffect(IncreaseActivatedAbilityCostEffect.opponentLoyalty(
                new PermanentIsPlaneswalkerPredicate(), 1));
    }
}
