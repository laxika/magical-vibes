package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Shuffles the controller's library, then repeatedly offers to exile its top card. If the
 * controller stops with total exiled mana value at most thirteen, they may cast any number of
 * the exiled spells without paying their mana costs.
 */
public record DanceWithCalamityEffect(List<UUID> exiledCardIds, int totalManaValue)
        implements CardEffect {

    public DanceWithCalamityEffect() {
        this(List.of(), 0);
    }

    public DanceWithCalamityEffect {
        exiledCardIds = List.copyOf(exiledCardIds);
    }
}
