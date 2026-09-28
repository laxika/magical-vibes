package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Randomly selects one card matching {@code predicate} from the controller's library and puts it
 * into that player's graveyard, then shuffles the library.
 */
public record SeekFromLibraryToGraveyardEffect(CardPredicate predicate) implements CardEffect {
}
