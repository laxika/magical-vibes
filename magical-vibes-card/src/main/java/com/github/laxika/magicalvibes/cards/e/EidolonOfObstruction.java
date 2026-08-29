package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "THB", collectorNumber = "12")
public class EidolonOfObstruction extends Card {

    public EidolonOfObstruction() {
        addEffect(EffectSlot.STATIC, IncreaseActivatedAbilityCostEffect.opponentLoyalty(
                new PermanentIsPlaneswalkerPredicate(), 1));
    }
}
