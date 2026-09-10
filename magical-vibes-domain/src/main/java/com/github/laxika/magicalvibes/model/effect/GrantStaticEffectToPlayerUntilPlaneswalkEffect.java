package com.github.laxika.magicalvibes.model.effect;

/**
 * Stamps a static effect onto the ability controller as a player-scoped effect until a player
 * planeswalks. The stamped effect is independent of the source planar card's continued presence.
 *
 * @param staticEffect static effect to apply to the ability controller's player scope
 */
public record GrantStaticEffectToPlayerUntilPlaneswalkEffect(CardEffect staticEffect)
        implements CardEffect {
}
