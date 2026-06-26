package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "This permanent enters tapped unless you control a permanent matching the predicate."
 * Used by the M10/M11 "check land" cycle (e.g. Dragonskull Summit with
 * {@code PermanentHasAnySubtypePredicate(Set.of(SWAMP, MOUNTAIN))}).
 */
public record EntersTappedUnlessControlsPermanentEffect(PermanentPredicate predicate) implements ReplacementEffect {
}
