package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSOS", collectorNumber = "25")
public class ScalarScholar extends Card {

    public ScalarScholar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyReduceCostForMatchingOwnedCardsEffect(
                        new CardHasXInManaCostPredicate(), 1));
    }
}
