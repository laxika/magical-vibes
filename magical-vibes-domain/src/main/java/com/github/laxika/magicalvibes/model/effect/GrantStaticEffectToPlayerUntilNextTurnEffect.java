package com.github.laxika.magicalvibes.model.effect;

/**
 * Stamps a static effect onto the ability controller as a player-scoped effect until their next turn.
 * The stamped effect is independent of the source permanent's continued presence.
 *
 * @param staticEffect static effect to apply to the ability controller's player scope
 */
public record GrantStaticEffectToPlayerUntilNextTurnEffect(CardEffect staticEffect) implements CardEffect {
}
