package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Set;

/**
 * Static effect: players can't cast spells from any zone listed in {@code zones}.
 * Prevents flashback, graveyard cast, casting from the top of a library, and any other mechanism
 * that casts spells from the listed zones. Does not prevent playing lands from those zones
 * (lands are not spells).
 * <p>
 * Only {@link Zone#GRAVEYARD}, {@link Zone#LIBRARY}, and {@link Zone#EXILE} are currently enforced
 * (those are the cast-from-zone gating sites wired up).
 * <p>
 * When {@code appliesToAllPlayers} is false, only opponents of the source permanent's controller
 * are restricted. Used by Ashes of the Abhorrent (XLN), which supplies {@code Set.of(GRAVEYARD)},
 * Grafdigger's Cage (DKA), which supplies {@code Set.of(GRAVEYARD, LIBRARY)}, and Drannith
 * Magistrate (IKO), which supplies all zones except {@code HAND} with {@code false}.
 */
public record PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones, boolean appliesToAllPlayers)
        implements CardEffect {

    public PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones) {
        this(zones, true);
    }
}
