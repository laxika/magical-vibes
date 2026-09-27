package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "52")
public class InfernoProject extends Card {

    public InfernoProject() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new TotalManaValueOfCardsInGraveyard(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))),
                                CountScope.CONTROLLER)));
    }
}
