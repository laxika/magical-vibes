package com.github.laxika.magicalvibes.model.effect;

/**
 * "Prevent the next N damage that would be dealt to you this turn." Non-targeting: shields the
 * ability's controller (Esper Battlemage). Adds to {@code GameData.playerDamagePreventionShields},
 * consumed from any source (combat or noncombat) by
 * {@code DamagePreventionService.applyPlayerPreventionShield}, and cleared at end of turn.
 */
public record PreventNextDamageToControllerEffect(int amount) implements CardEffect {
}
