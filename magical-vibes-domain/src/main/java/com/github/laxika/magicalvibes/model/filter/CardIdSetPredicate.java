package com.github.laxika.magicalvibes.model.filter;

import java.util.Set;
import java.util.UUID;

/**
 * Matches the specified card identities. Optional graveyard versions restrict each identity to
 * its current stay in the graveyard, so leaving and returning invalidates a delayed return.
 */
public record CardIdSetPredicate(Set<UUID> cardIds, java.util.Map<UUID, Long> graveyardVersions) implements CardPredicate {
    public CardIdSetPredicate(Set<UUID> cardIds) {
        this(cardIds, java.util.Map.of());
    }

    public CardIdSetPredicate {
        cardIds = Set.copyOf(cardIds);
        graveyardVersions = java.util.Map.copyOf(graveyardVersions);
    }
}
