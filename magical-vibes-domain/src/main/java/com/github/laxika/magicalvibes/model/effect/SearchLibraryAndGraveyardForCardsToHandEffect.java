package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import java.util.Arrays;
import java.util.List;

/**
 * "Search your library and graveyard for [a card of each description], reveal those cards, put them into your hand,
 * then shuffle": up to one card matching each of {@code cards}, in that order. Both zones are searched (an "and"
 * search asks no zones, unlike an "and/or" one), the library once for all the descriptions (CR 701.23h), whatever is
 * found. A description matched only in the graveyard, a public zone, is taken from it; one matched in the library is a
 * pick that also offers the graveyard's matches and can find nothing only when the graveyard has none (CR 701.23b).
 * "Then shuffle" shuffles even a library that couldn't be searched (the Leonin Arbiter ruling).
 *
 * @param cards the descriptions searched for, one card each
 */
public record SearchLibraryAndGraveyardForCardsToHandEffect(List<CardPredicate> cards) implements CardEffect {

    public SearchLibraryAndGraveyardForCardsToHandEffect {
        cards = List.copyOf(cards);
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("A search looks for at least one card");
        }
    }

    /** "Search your library and graveyard for a card named A, a card named B, ...". */
    public static SearchLibraryAndGraveyardForCardsToHandEffect named(String... cardNames) {
        return new SearchLibraryAndGraveyardForCardsToHandEffect(Arrays.stream(cardNames)
                .<CardPredicate>map(CardNamedPredicate::new).toList());
    }
}
