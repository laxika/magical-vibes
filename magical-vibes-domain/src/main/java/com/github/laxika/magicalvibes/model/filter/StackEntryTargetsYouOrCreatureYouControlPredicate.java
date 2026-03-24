package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells or abilities that target the counterspell's controller (the player)
 * or a creature controlled by that player. Used by Siren Stormtamer and similar cards
 * ("Counter target spell or ability that targets you or a creature you control").
 */
public record StackEntryTargetsYouOrCreatureYouControlPredicate() implements StackEntryPredicate {
}
