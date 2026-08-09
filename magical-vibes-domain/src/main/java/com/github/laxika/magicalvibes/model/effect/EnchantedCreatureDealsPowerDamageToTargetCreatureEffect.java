package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature enchanted by the source Aura deals damage equal to its power to target creature.
 */
public record EnchantedCreatureDealsPowerDamageToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
