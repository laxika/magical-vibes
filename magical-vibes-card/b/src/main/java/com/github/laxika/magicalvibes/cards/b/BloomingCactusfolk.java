package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YOTJ", collectorNumber = "16")
public class BloomingCactusfolk extends Card {

    public BloomingCactusfolk() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PerpetuallyReduceCostForMatchingHandCardsEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 1));
    }
}
