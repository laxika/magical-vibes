package com.github.laxika.magicalvibes.model.effect;

/**
 * Queues a targeted creature to attack during its controller's next combat phase if able.
 */
public record TargetCreatureMustAttackNextCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
