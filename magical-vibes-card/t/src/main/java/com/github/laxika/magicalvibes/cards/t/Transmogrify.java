package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "M21", collectorNumber = "167")
@CardRegistration(set = "M21", collectorNumber = "370")
public class Transmogrify extends Card {

    public Transmogrify() {
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                        new CardTypePredicate(CardType.CREATURE),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect.Players(
                                Subjects.controllerOf(Subjects.target()))).shufflingRest());
    }
}
