package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoGraveyardFromBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "24")
@CardRegistration(set = "DMC", collectorNumber = "74")
public class ActivatedSleeper extends Card {

    public ActivatedSleeper() {
        addStaticEffect(CopyCreatureCardInGraveyardOnEnterEffect.of(Subjects.cardsInGraveyards(Subjects.eachPlayer(),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPutIntoGraveyardFromBattlefieldThisTurnPredicate()))))
                .except(CopyExceptions.NONE.alsoWithSubtypes(CardSubtype.PHYREXIAN)));
    }
}
