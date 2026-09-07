package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents damage to the target creature when a spell or ability that targets it would cause
 * that damage.
 */
public record PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
