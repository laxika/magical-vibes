package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures that are attacking the source's controller (i.e. "attacking you"). A creature
 * attacking a planeswalker or a different player is not matched — only attackers whose declared
 * attack target is the source controller. Requires a {@link FilterContext} with a source controller.
 */
public record PermanentIsAttackingSourceControllerPredicate() implements PermanentPredicate {
}
