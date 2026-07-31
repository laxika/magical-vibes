package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhyrexianPortalEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "126")
public class PhyrexianPortal extends Card {

    public PhyrexianPortal() {
        // {3}: If your library has ten or more cards in it, target opponent looks at the top ten cards
        // of your library and separates them into two face-down piles. Exile one of those piles. Search
        // the other pile for a card, put it into your hand, then shuffle the rest of that pile into
        // your library.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PhyrexianPortalEffect()),
                "{3}: If your library has ten or more cards in it, target opponent looks at the top ten "
                        + "cards of your library and separates them into two face-down piles. Exile one of "
                        + "those piles. Search the other pile for a card, put it into your hand, then "
                        + "shuffle the rest of that pile into your library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
