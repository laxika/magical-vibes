package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the source permanent to be returned to its owner's hand at end of
 * combat. The delayed action does nothing if the source has already left the battlefield.
 */
public record ReturnSelfToHandAtEndOfCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
