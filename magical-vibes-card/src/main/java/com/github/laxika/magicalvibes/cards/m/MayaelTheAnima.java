package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "179")
public class MayaelTheAnima extends Card {

    public MayaelTheAnima() {
        // {3}{R}{G}{W}, {T}: Look at the top five cards of your library. You may put a creature
        // card with power 5 or greater from among them onto the battlefield. Put the rest on the
        // bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{R}{G}{W}",
                List.of(LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(5, new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPowerAtLeastPredicate(5)
                )))),
                "{3}{R}{G}{W}, {T}: Look at the top five cards of your library. You may put a creature "
                        + "card with power 5 or greater from among them onto the battlefield. Put the rest "
                        + "on the bottom of your library in any order."
        ));
    }
}
