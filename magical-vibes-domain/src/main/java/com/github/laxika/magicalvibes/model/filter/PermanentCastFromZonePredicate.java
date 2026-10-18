package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * Permanents whose spell was cast from the given zone, as recorded on the permanent when it entered
 * from the stack. A permanent that entered without being cast never matches.
 */
public record PermanentCastFromZonePredicate(Zone zone) implements PermanentPredicate {
}
