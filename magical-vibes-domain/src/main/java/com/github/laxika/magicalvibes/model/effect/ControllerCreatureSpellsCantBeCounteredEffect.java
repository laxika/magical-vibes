package com.github.laxika.magicalvibes.model.effect;

/**
 * "Creature spells you control with power {@code minimumPower} or greater can't be countered."
 * Static ability on the source permanent — read directly by {@code GameQueryService.isUncounterable}.
 * Registered in {@code EffectSlot.STATIC}.
 *
 * <p>Unlike {@link CreatureSpellsCantBeCounteredEffect} (which protects every creature spell,
 * regardless of controller or power), this protects only creature spells controlled by the source
 * permanent's controller whose power is at least {@code minimumPower} (Spellbreaker Behemoth: 5).
 */
public record ControllerCreatureSpellsCantBeCounteredEffect(int minimumPower) implements CardEffect {
}
