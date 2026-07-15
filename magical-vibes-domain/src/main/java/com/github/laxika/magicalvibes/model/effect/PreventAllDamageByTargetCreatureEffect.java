package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all damage the target creature(s) would deal this turn (multi-target via targetIds).
 * When {@code combatOnly} is {@code true}, only combat damage is prevented (Resistance Fighter);
 * otherwise all damage the creature would deal is prevented (Soul Parry, Inquisitor's Snare).
 */
public record PreventAllDamageByTargetCreatureEffect(boolean combatOnly) implements CardEffect {

    public PreventAllDamageByTargetCreatureEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
