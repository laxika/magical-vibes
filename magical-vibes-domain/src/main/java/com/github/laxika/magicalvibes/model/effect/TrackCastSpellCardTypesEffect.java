package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.Set;

/** Checks newly cast spell types on a source's tracker, draws once, and sacrifices a completed tracker. */
public record TrackCastSpellCardTypesEffect(Set<CardType> trackedTypes, Set<CardType> castTypes)
        implements CardEffect {
    public TrackCastSpellCardTypesEffect {
        trackedTypes = Set.copyOf(trackedTypes);
        castTypes = Set.copyOf(castTypes);
    }

    public TrackCastSpellCardTypesEffect(Set<CardType> trackedTypes) {
        this(trackedTypes, Set.of());
    }
}
