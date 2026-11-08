package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose card the evaluating source, an Aura that enchants a card in a hand, is attached to: "when you
 * cast enchanted card" (Don't Worry About It). Source-dependent: evaluated via
 * {@code TargetLegalityService.matchesStackEntryPredicate(..., source)}.
 */
public record StackEntryIsCardEnchantedBySourcePredicate() implements StackEntryPredicate {
}
