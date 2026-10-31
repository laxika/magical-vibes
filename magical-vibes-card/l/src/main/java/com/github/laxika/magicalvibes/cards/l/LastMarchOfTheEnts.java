package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOC", collectorNumber = "27")
@CardRegistration(set = "HOC", collectorNumber = "67")
public class LastMarchOfTheEnts extends Card {

    public LastMarchOfTheEnts() {
        addStaticEffect(new CantBeCounteredEffect());

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addSpellEffect(new DrawCardEffect(new GreatestToughnessAmongControlled()));
        addSpellEffect(new PutUpToCardsFromHandOntoBattlefieldEffect(
                creature,
                "creature",
                new MatchingCardsInHand(CountScope.CONTROLLER, creature)));
    }
}
