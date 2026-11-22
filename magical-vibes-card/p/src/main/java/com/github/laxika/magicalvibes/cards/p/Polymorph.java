package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "M10", collectorNumber = "67")
@CardRegistration(set = "9ED", collectorNumber = "90")
@CardRegistration(set = "6ED", collectorNumber = "86")
@CardRegistration(set = "MIR", collectorNumber = "82")
public class Polymorph extends Card {

    public Polymorph() {
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect(true))
                .addSpellEffect(RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                        new CardTypePredicate(CardType.CREATURE),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect.Players(
                                Subjects.controllerOf(Subjects.target()))).shufflingRest());
    }
}
