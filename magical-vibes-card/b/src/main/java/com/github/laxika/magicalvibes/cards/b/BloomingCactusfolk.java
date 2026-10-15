package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YOTJ", collectorNumber = "16")
public class BloomingCactusfolk extends Card {

    public BloomingCactusfolk() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PerpetuallyReduceCostForMatchingHandCardsEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 1));
    }
}
