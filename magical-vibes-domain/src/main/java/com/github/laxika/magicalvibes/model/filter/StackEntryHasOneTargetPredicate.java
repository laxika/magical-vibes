package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell or ability with a single target: exactly one target, chosen once. Unlike
 * {@link StackEntryIsSingleTargetPredicate}, a spell that names the same object or player in several of its targets
 * (Seeds of Strength on one creature three times) doesn't match. Glimmervoid Basin's "an instant or sorcery spell with
 * a single target", a cast trigger's stack-entry filter.
 */
public record StackEntryHasOneTargetPredicate() implements StackEntryPredicate {
}
