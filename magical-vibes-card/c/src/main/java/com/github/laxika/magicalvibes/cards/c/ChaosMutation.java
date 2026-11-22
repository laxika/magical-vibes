package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "111")
public class ChaosMutation extends Card {

    public ChaosMutation() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creature(), 0, 99)
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                        new CardTypePredicate(CardType.CREATURE),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect.ControllerOfEachExiledThisWay()));
    }
}
