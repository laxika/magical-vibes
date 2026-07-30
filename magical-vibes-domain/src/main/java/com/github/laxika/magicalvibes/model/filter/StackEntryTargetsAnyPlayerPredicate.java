package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells or abilities that target at least one player (any player, not just the
 * evaluating source's controller). Used by Outwit ("counter target spell that targets a
 * player"). Contrast with {@link StackEntryTargetsYouPredicate}, which only matches spells
 * targeting the evaluating controller.
 */
public record StackEntryTargetsAnyPlayerPredicate() implements StackEntryPredicate {
}
