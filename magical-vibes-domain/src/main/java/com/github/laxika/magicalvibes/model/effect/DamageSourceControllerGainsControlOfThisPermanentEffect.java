package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a permanent deals damage to this effect's controller, the damage source's controller
 * gains control of this permanent (the permanent that has this effect).
 * Used with {@code ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU}, or with
 * {@code ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU} inside a
 * {@link BatchedCombatDamageToYouTriggerEffect} for an ability triggered by one or more creatures.
 *
 * @param combatOnly   if true, only combat damage triggers the control change (not ability/spell damage)
 * @param creatureOnly if true, only damage from creatures triggers the control change
 * @param untap        if true, untap this permanent after the control change
 */
public record DamageSourceControllerGainsControlOfThisPermanentEffect(
        boolean combatOnly,
        boolean creatureOnly,
        boolean untap
) implements CardEffect {

    public DamageSourceControllerGainsControlOfThisPermanentEffect(boolean combatOnly, boolean creatureOnly) {
        this(combatOnly, creatureOnly, false);
    }
}
