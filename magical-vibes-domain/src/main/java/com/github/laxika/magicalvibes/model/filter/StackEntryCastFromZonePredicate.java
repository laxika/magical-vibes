package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * Matches stack entries for spells that were cast from the given zone (via the entry's {@code sourceZone}). Used for
 * "whenever a player casts a spell from a graveyard" triggers (e.g. River Kelpie), which tell graveyard casts
 * (flashback, etc.) from exile casts. {@link Zone#HAND} reads "from your hand" (or "their hand"), its caster's: a spell
 * cast from another player's hand (Sen Triplets) wasn't, so Vega the Watcher sees it as cast from elsewhere. "From its
 * owner's hand" (Wash Away) is {@link StackEntryCastFromOwnersHandPredicate}.
 */
public record StackEntryCastFromZonePredicate(Zone sourceZone) implements StackEntryPredicate {
}
