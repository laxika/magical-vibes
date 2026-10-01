package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/** Perpetually changes matching cards in hand and library into creatures with fixed characteristics. */
public record PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect(
        CardPredicate filter,
        int power,
        int toughness,
        CardSubtype subtype,
        Set<Keyword> keywords) implements CardEffect {

    public PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect {
        keywords = Set.copyOf(keywords);
    }
}
