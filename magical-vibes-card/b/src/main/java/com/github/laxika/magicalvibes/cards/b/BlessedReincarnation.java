package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "DTK", collectorNumber = "47")
public class BlessedReincarnation extends Card {

    public BlessedReincarnation() {
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                        new CardTypePredicate(CardType.CREATURE),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect.Players(
                                Subjects.controllerOf(Subjects.target()))).shufflingRest());
    }
}
