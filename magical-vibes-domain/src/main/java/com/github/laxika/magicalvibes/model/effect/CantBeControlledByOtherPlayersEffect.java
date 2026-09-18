package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: other players can't gain control of the permanent carrying this effect.
 * Read by the central control-change service; normally granted through {@link GrantEffectEffect}.
 */
public record CantBeControlledByOtherPlayersEffect() implements CardEffect {
}
