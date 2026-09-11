package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/** Returns cards tracked by the source's source-linked exile-return entries after it untaps. */
public record ReturnCardsExiledWithSourceOnUntapEffect(Set<UUID> primaryCardIds) implements CardEffect {

    public ReturnCardsExiledWithSourceOnUntapEffect() {
        this(Set.of());
    }

    public ReturnCardsExiledWithSourceOnUntapEffect {
        primaryCardIds = primaryCardIds == null ? Set.of() : Set.copyOf(primaryCardIds);
    }
}
