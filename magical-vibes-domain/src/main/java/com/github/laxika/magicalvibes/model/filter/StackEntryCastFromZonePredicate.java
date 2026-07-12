package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * Matches stack entries for spells that were cast from the given zone (via the entry's
 * {@code sourceZone}). Used for "whenever a player casts a spell from a graveyard" triggers
 * (e.g. River Kelpie) — distinguishes graveyard casts (flashback, etc.) from exile casts.
 */
public record StackEntryCastFromZonePredicate(Zone sourceZone) implements StackEntryPredicate {
}
