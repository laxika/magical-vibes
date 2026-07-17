package com.github.laxika.magicalvibes.model.effect;

/**
 * Sylvan Library's draw-step ability (resolved as the wrapped effect of a {@code MayEffect}):
 * the controller draws two additional cards, then chooses two cards in their hand drawn this
 * turn; for each of those cards they pay 4 life or put the card on top of their library.
 *
 * <p>The "you may" (whether to draw the two extra cards at all) is the enclosing {@code MayEffect};
 * this effect always runs the mandatory follow-up once the draws happen.
 */
public record SylvanLibraryDrawEffect() implements CardEffect {
}
