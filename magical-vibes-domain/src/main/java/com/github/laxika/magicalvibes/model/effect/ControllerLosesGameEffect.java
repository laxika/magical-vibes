package com.github.laxika.magicalvibes.model.effect;

/**
 * "You lose the game." — the controller of the resolving ability loses the game.
 *
 * <p>Non-targeting. Unlike {@link TargetPlayerLosesGameEffect} (which is bound to a specific player
 * at construction), this resolves against the stack entry's controller, so it can be used by
 * state-triggered / dynamic-controller abilities (e.g. Immortal Coil's empty-graveyard loss).
 */
public record ControllerLosesGameEffect() implements CardEffect {
}
