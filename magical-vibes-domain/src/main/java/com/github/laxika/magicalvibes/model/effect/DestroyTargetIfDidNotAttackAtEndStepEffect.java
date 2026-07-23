package com.github.laxika.magicalvibes.model.effect;

/**
 * Queues destruction of the targeted permanent at the beginning of the next end step if it did not
 * attack this turn. Pair with {@link MustAttackThisTurnEffect} for Norritt's
 * "attacks this turn if able; destroy it at the next end step if it didn't attack".
 */
public record DestroyTargetIfDidNotAttackAtEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
