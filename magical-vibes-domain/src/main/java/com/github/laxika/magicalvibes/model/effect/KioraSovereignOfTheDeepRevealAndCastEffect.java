package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top cards of a library and offers a spell with mana value less than the triggering
 * spell's mana value for a free cast.
 *
 * @param spellManaValue mana value of the spell that caused the trigger
 */
public record KioraSovereignOfTheDeepRevealAndCastEffect(int spellManaValue) implements CardEffect {
}
