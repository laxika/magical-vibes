package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top card of your library and put that card into your hand. You lose life equal to
 * its mana value. You may repeat this process any number of times.
 *
 * <p>The first reveal is mandatory (if the library is non-empty); after each revealed card the
 * controller decides whether to repeat, via a
 * {@link com.github.laxika.magicalvibes.model.PendingInteraction.AdNauseamRepeatChoice}
 * accept/decline prompt (CR: you decide whether to continue after each card, not in advance).
 * Used by Ad Nauseam.
 */
public record AdNauseamEffect() implements CardEffect {
}
