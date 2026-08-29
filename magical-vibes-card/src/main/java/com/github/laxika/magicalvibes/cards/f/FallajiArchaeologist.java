package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "48")
public class FallajiArchaeologist extends Card {

    public FallajiArchaeologist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
                        3, new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
