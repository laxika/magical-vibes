package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the source card from exile to the battlefield under its owner's control.
 *
 * @param tapped whether the returned permanent enters tapped
 */
public record ReturnSourceCardFromExileToBattlefieldEffect(boolean tapped) implements CardEffect {
}
