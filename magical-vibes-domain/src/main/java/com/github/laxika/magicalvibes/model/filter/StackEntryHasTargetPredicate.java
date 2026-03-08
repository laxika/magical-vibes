package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches any spell or ability on the stack. Also signals to
 * {@code TargetLegalityService} to include triggered and activated abilities
 * in the stack search (not just spells). Used by cards like Spellskite that
 * target "target spell or ability."
 */
public record StackEntryHasTargetPredicate() implements StackEntryPredicate {
}
