package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns one of the permanents of a "one or more creatures deal combat damage" event to its owner's hand, then
 * resolves the follow-up only after a permanent was returned. The permanents are the stack entry's triggering
 * permanents, which the combat damage collector sets as the ability triggers.
 */
public record ReturnOneOfCombatDamageDealersToHandThenEffect(
        CardEffect thenEffect,
        String permanentDescription
) implements CardEffect {
}
