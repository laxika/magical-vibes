package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile a fixed number of cards from the top of your library. Grants play permission
 * for those cards (of any type) until the end of your next turn.
 * <p>
 * Used by cards like Elemental Mascot: "exile the top card of your library. You may play
 * that card until the end of your next turn."
 */
public record ExileTopCardsMayPlayUntilNextTurnEffect(int count) implements CardEffect {
}
