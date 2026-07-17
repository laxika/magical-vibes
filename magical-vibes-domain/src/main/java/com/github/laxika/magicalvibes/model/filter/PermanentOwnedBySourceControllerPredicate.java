package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents owned by the source's controller (ownership tracked via the
 * {@code stolenCreatures} map). Pairs with {@link ControlledPermanentPredicateTargetFilter}
 * to express "target permanent you both own and control" (Obelisk of Undoing).
 */
public record PermanentOwnedBySourceControllerPredicate() implements PermanentPredicate {
}
