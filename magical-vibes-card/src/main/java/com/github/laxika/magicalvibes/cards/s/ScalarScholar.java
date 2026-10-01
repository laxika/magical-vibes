package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;

@CardRegistration(set = "YSOS", collectorNumber = "25")
public class ScalarScholar extends Card {

    public ScalarScholar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyReduceCostForMatchingOwnedCardsEffect(
                        new CardHasXInManaCostPredicate(), 1));
    }
}
