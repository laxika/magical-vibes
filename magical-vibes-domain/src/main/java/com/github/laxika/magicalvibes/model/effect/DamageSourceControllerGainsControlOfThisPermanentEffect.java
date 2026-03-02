package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a permanent deals damage to this effect's controller, the damage source's controller
 * gains control of this permanent (the permanent that has this effect).
 * Used with EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.
 *
 * @param combatOnly   if true, only combat damage triggers the control change (not ability/spell damage)
 * @param creatureOnly if true, only damage from creatures triggers the control change
 */
public record DamageSourceControllerGainsControlOfThisPermanentEffect(
        boolean combatOnly,
        boolean creatureOnly
) implements CardEffect {
}
