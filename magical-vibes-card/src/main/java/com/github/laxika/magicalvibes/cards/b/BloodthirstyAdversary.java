package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAnyNumberOfTimesPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "129")
public class BloodthirstyAdversary extends Card {

    public BloodthirstyAdversary() {
        CardPredicate instantOrSorceryWithManaValueAtMostThree = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))),
                new CardMaxManaValuePredicate(3)));
        ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect exileAndCopy =
                new ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect(true);

        targetUpTo(new XValue(), new GraveyardCardPredicateTargetFilter(
                instantOrSorceryWithManaValueAtMostThree, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 100);
        registerEffectTargetIndex(exileAndCopy, 0);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayManaAnyNumberOfTimesPutCountersOnSelfEffect(
                        "{2}{R}", CounterType.PLUS_ONE_PLUS_ONE, exileAndCopy));
    }
}
