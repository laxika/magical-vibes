package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Set;

/**
 * Static effect: no player can cast spells from any zone listed in {@code zones}.
 * Prevents flashback, graveyard cast, casting from the top of a library, and any other mechanism
 * that casts spells from the listed zones. Does not prevent playing lands from those zones
 * (lands are not spells).
 * <p>
 * Only {@link Zone#GRAVEYARD} and {@link Zone#LIBRARY} are currently enforced (those are the only
 * cast-from-zone gating sites wired up).
 * <p>
 * Used by Ashes of the Abhorrent (XLN), which supplies {@code Set.of(GRAVEYARD)}, and Grafdigger's
 * Cage (DKA), which supplies {@code Set.of(GRAVEYARD, LIBRARY)}.
 */
public record PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones) implements CardEffect {
}
