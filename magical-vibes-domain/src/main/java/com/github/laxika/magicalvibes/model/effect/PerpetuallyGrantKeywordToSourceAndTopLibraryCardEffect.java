package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Perpetually grants a keyword to the source card and to the first matching card in its
 * controller's library. The library filter is evaluated from the top of the library downward.
 */
public record PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect(
        Keyword keyword,
        CardPredicate libraryCardFilter
) implements CardEffect {
}
