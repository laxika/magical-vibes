package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures a duplicate of the nontoken creature that dealt the combat damage into hand: the stack entry's triggering
 * permanent as it dealt the damage (an equipped creature), else the ability's source.
 */
public record ConjureDuplicateOfCombatDamageSourceIntoHandEffect() implements CardEffect {
}
