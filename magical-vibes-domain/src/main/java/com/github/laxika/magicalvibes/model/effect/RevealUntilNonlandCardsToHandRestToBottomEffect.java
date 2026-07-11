package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the caster's library until they reveal {@code count} nonland cards.
 * The nonland cards are put into the caster's hand, then the rest of the revealed cards (the lands)
 * are put on the bottom of the library in any order.
 * <p>
 * Used by Fathom Trawl.
 */
public record RevealUntilNonlandCardsToHandRestToBottomEffect(int count) implements CardEffect {
}
