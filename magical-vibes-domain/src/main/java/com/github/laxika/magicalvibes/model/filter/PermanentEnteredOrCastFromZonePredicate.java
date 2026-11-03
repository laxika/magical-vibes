package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * Permanents that came from the given zone: put onto the battlefield from it, or resolved from a spell cast from it
 * ("if that creature entered from a graveyard or you cast it from a graveyard").
 */
public record PermanentEnteredOrCastFromZonePredicate(Zone zone) implements PermanentPredicate {
}
