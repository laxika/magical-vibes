package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells or abilities that target the evaluating source permanent.
 * Used by Mistfolk and similar cards ("Counter target spell that targets this creature").
 * Source-dependent: matches nothing unless the source permanent is passed to
 * {@code TargetLegalityService.matchesStackEntryPredicate(..., source)}; the ability-activation
 * path supplies it automatically.
 */
public record StackEntryTargetsSourcePredicate() implements StackEntryPredicate {
}
