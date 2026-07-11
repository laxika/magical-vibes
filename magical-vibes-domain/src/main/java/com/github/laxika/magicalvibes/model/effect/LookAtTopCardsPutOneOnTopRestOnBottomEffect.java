package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of your library, put one of those cards on top of your
 * library and the rest on the bottom of your library in any order. The "look" itself is private;
 * only the fact that a card was placed on top is logged (not its identity). Used by the
 * materialised Cream of the Crop trigger, where {@code count} is the entering creature's power
 * (see {@link LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect}).
 */
public record LookAtTopCardsPutOneOnTopRestOnBottomEffect(int count) implements CardEffect {
}
