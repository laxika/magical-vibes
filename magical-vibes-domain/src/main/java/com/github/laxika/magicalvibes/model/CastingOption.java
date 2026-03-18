package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Optional;

/**
 * An alternate way to cast a spell. Each subtype represents a specific keyword mechanic
 * (flashback, alternate hand cast, etc.) with its own fixed zone and disposition rules.
 */
public sealed interface CastingOption permits FlashbackCast, AlternateHandCast, GraveyardCast {

    Disposition disposition();

    List<CastingCost> costs();

    default <T extends CastingCost> Optional<T> getCost(Class<T> type) {
        return costs().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }
}
