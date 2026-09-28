package com.github.laxika.magicalvibes.model.filter;

import java.util.Set;
import java.util.UUID;

/** Matches only the exact card instances identified by their UUIDs. */
public record CardIdSetPredicate(Set<UUID> cardIds) implements CardPredicate {

    public CardIdSetPredicate {
        cardIds = Set.copyOf(cardIds);
    }
}
