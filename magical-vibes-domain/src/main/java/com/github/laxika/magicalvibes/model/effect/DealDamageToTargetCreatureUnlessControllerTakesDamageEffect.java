package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted creature's controller chooses whether the source deals damage to them or the
 * targeted creature is dealt damage.
 */
public record DealDamageToTargetCreatureUnlessControllerTakesDamageEffect(
        int targetDamage, int controllerDamage) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
