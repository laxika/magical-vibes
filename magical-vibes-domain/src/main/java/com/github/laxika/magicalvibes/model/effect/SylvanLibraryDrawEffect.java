package com.github.laxika.magicalvibes.model.effect;

/**
 * Sylvan Library's follow-up after its two additional draw effects: the controller chooses
 * two cards in their hand drawn this
 * turn; for each of those cards they pay 4 life or put the card on top of their library.
 *
 * <p>The "you may" (whether to draw the two extra cards at all) is the enclosing {@code MayEffect};
 * this effect runs after both draws and their replacement choices have finished.
 */
public record SylvanLibraryDrawEffect() implements CardEffect {
}
