package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller discards two cards unless they discard a creature card instead.
 *
 * <p>Choosing the two-card option allows any two cards, including creature cards. If the
 * controller has no creature card when this effect resolves, discarding two cards is mandatory.
 */
public record DiscardTwoUnlessCreatureEffect() implements CardEffect {
}
