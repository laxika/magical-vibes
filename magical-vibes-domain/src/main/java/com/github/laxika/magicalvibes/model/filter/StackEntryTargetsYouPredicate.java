package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells or abilities that target the evaluating source's controller (the player).
 * Used by Mirror Sheen and similar cards ("... target instant or sorcery spell that targets you").
 */
public record StackEntryTargetsYouPredicate() implements StackEntryPredicate {
}
