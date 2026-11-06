package com.github.laxika.magicalvibes.model.effect;

/**
 * "Conjure a duplicate of one of them into your hand": chooses one of the creatures of a "one or more creatures deal
 * combat damage" event and conjures a duplicate of it into the controller's hand. The creatures are the stack entry's
 * triggering permanents, which the combat damage collector sets as the ability triggers.
 */
public record ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect() implements CardEffect {
}
