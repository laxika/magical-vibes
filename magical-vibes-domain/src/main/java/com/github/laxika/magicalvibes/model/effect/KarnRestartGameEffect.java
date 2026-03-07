package com.github.laxika.magicalvibes.model.effect;

/**
 * Restarts the game (CR 727). Non-Aura permanent cards exiled with the source permanent
 * remain in exile, then are put onto the battlefield under the controller's control.
 * Used by Karn Liberated's ultimate ability.
 */
public record KarnRestartGameEffect() implements CardEffect {
}
