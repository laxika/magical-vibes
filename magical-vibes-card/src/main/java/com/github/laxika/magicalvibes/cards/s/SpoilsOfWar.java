package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "164")
public class SpoilsOfWar extends Card {

    public SpoilsOfWar() {
        // X is the number of artifact and/or creature cards in an opponent's graveyard as you cast
        // this spell. Distribute X +1/+1 counters among any number of target creatures.
        // The total is evaluated at cast time and the division is announced then, riding on the
        // stack entry's assignment map (the counter analogue of Jaws of Stone).
        addEffect(EffectSlot.SPELL, DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CardsInGraveyard(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        CountScope.OPPONENTS)));
    }
}
