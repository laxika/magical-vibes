package com.github.laxika.magicalvibes.model.effect;

/**
 * Lets the controller choose one of the creatures of a "one or more creatures deal combat damage" event to sacrifice,
 * then reveals until a creature card sharing a creature type with it is found. The creatures are the stack entry's
 * triggering permanents, which the combat damage collector sets as the ability triggers.
 */
public record SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect() implements CardEffect {
}
