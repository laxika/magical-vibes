package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Randomly selects one card matching {@code predicate} from the controller's library and puts it
 * into that player's hand, then shuffles the library.
 */
public record SeekFromLibraryToHandEffect(CardPredicate predicate) implements CardEffect {
}
