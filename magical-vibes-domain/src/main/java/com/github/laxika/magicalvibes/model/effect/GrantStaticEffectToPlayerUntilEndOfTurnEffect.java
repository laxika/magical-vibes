package com.github.laxika.magicalvibes.model.effect;

/**
 * Stamps a static effect onto the ability controller as a player-scoped effect until end of turn.
 * The stamped effect is independent of the source permanent's continued presence.
 *
 * @param staticEffect static effect to apply to the ability controller's player scope
 */
public record GrantStaticEffectToPlayerUntilEndOfTurnEffect(CardEffect staticEffect) implements CardEffect {
}
