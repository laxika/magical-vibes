package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ARB", collectorNumber = "131")
public class ArsenalThresher extends Card {

    public ArsenalThresher() {
        // As this creature enters, you may reveal any number of other artifact cards from your
        // hand. This creature enters with a +1/+1 counter on it for each card revealed this way.
        // Revealing has no downside, so it is modelled as the whole set of qualifying hand cards
        // (cf. Sacellum Godspeaker / Phosphorescent Feast). The source is already entering the
        // battlefield while the amount is evaluated, so it is not counted among the hand cards.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new MatchingCardsInHand(CountScope.CONTROLLER, new CardTypePredicate(CardType.ARTIFACT))));
    }
}
