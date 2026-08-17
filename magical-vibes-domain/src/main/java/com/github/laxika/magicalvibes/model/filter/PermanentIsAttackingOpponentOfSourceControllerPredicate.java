package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures attacking a player other than the source controller. Attacks against
 * planeswalkers or battles do not match. Requires a {@link FilterContext} with a source controller.
 */
public record PermanentIsAttackingOpponentOfSourceControllerPredicate() implements PermanentPredicate {
}
