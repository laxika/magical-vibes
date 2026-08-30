package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent may discard a card. An opponent who does not discard loses life instead.
 *
 * <p>The affected players make their choices in APNAP order. This effect is intentionally
 * separate from {@link LoseLifeUnlessDiscardEffect}: that effect represents one affected player,
 * while this effect expands the same choice across every opponent.
 */
public record EachOpponentMayDiscardOrLoseLifeEffect(int lifeLoss) implements CardEffect {
}
