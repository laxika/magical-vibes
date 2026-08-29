package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. A nonland card with mana value lower than
 * {@code spellManaValue} may be cast without paying its mana cost; otherwise, or if declined, the
 * revealed card is put into its controller's hand.
 *
 * @param spellManaValue mana value of the spell that caused Rashmi's ability to trigger
 */
public record RashmiRevealTopCardEffect(int spellManaValue) implements CardEffect {
}
