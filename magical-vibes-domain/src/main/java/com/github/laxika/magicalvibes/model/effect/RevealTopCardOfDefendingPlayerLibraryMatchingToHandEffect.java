package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the defending player's library. If it matches the predicate, that
 * player puts it into their hand; otherwise it remains on top of their library.
 *
 * <p>The defending player is captured by the attack trigger as combat context, not chosen as a
 * target. This is used for attack triggers such as Goblin Guide.
 */
public record RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect(CardPredicate matchPredicate)
        implements CardEffect {
}
