package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "4")
public class AvenWarhawk extends Card {

    public AvenWarhawk() {
        // Amplify 1 — This creature enters with a +1/+1 counter on it for each Bird and/or
        // Soldier card revealed from your hand. A card with both subtypes is counted once.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new MatchingCardsInHand(CountScope.CONTROLLER, new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.BIRD),
                        new CardSubtypePredicate(CardSubtype.SOLDIER)
                )))));
    }
}
