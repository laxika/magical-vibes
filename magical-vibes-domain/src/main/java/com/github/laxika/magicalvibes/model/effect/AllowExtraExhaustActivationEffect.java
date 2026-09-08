package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that lets its controller activate exhaust abilities as though they have not
 * been activated, while the controller is taking their turn and has not begun an exhaust
 * activation that turn.
 */
public record AllowExtraExhaustActivationEffect() implements CardEffect {
}
