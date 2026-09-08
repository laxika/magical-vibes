package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolves a permanent choice, then prevents all combat damage dealt by the target creature if
 * that creature shares a color with the chosen permanent.
 */
public record PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
