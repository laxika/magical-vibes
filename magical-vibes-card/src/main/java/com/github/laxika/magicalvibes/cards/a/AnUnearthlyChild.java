package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "35")
public class AnUnearthlyChild extends Card {

    private static final CardPredicate SEARCH_PREDICATE = new CardAnyOfPredicate(List.of(
            new CardSubtypePredicate(CardSubtype.DOCTOR),
            new CardKeywordPredicate(Keyword.DOCTORS_COMPANION),
            new CardSubtypePredicate(CardSubtype.VEHICLE)));

    public AnUnearthlyChild() {
        RevealUntilCardPredicateRestOnBottomRandomEffect search =
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        SEARCH_PREDICATE, LibrarySearchDestination.HAND);
        addEffect(EffectSlot.SAGA_CHAPTER_I, search);
        addEffect(EffectSlot.SAGA_CHAPTER_II, search);
        addEffect(EffectSlot.SAGA_CHAPTER_III, search);
    }
}
