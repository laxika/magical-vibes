package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top cards of the controller's library and distributes them between hand, the
 * bottom of the library, and exile, granting the exiled card permission to be played this turn.
 */
public record LookAtTopCardsHandBottomExileEffect(int count) implements CardEffect {
}
