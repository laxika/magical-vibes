package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.Set;
import java.util.UUID;

/**
 * "Prevent all damage a source of your choice would deal to you this turn" (CR 615): the chosen source, as the
 * floating effect on the player it protects (CR 611.2a) carries it. A spell that becomes a permanent is the same
 * source under both of its ids (CR 609.7a), so {@link #sourceIds} holds the card's and later the permanent's.
 *
 * @param sourceIds                   the ids the chosen source goes by
 * @param requiredColors              the colours the source must still have for the prevention to apply, empty for
 *                                    none (CR 609.7b); a source that no longer has one of them is not prevented
 * @param gainLifeForBlackOrRedSource whether the player gains life for damage of a black or red source prevented
 *                                    this way (Samite Ministration)
 */
public record PlayerSourceDamagePrevention(Set<UUID> sourceIds, Set<CardColor> requiredColors,
                                           boolean gainLifeForBlackOrRedSource) implements CardEffect {

    public PlayerSourceDamagePrevention {
        sourceIds = Set.copyOf(sourceIds);
        requiredColors = Set.copyOf(requiredColors);
    }

    /** The same prevention, also naming {@code additionalSourceId} as the source. */
    public PlayerSourceDamagePrevention alsoNaming(UUID additionalSourceId) {
        Set<UUID> ids = new java.util.HashSet<>(sourceIds);
        ids.add(additionalSourceId);
        return new PlayerSourceDamagePrevention(ids, requiredColors, gainLifeForBlackOrRedSource);
    }
}
