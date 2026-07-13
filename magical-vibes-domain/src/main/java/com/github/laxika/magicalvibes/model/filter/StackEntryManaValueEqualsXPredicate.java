package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells on the stack whose mana value equals X, where X comes from the
 * casting spell's chosen X at targeting time. Used for "counter target spell with
 * mana value X" (Spell Blast). Evaluated in the targeting context by
 * {@code TargetLegalityService}; when X is unknown (target enumeration before X is
 * chosen) it is treated permissively.
 */
public record StackEntryManaValueEqualsXPredicate() implements StackEntryPredicate {
}
