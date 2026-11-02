package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "35")
@CardRegistration(set = "WHO", collectorNumber = "640")
public class AnUnearthlyChild extends Card {

    private static final CardPredicate SEARCH_PREDICATE = new CardAnyOfPredicate(List.of(
            new CardSubtypePredicate(CardSubtype.DOCTOR),
            new CardKeywordPredicate(Keyword.DOCTORS_COMPANION),
            new CardSubtypePredicate(CardSubtype.VEHICLE)));

    public AnUnearthlyChild() {
        RevealUntilCardPredicateRestOnBottomRandomEffect search =
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        SEARCH_PREDICATE, LibrarySearchDestination.HAND);
        addTrigger(Triggers.chapter(1), search);
        addTrigger(Triggers.chapter(2), search);
        addTrigger(Triggers.chapter(3), search);
    }
}
