package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Static blocking restriction: this creature can't block unless the given {@link Condition} is met.
 * Evaluated at block-declaration legality time in {@code GameQueryService.canBlock} via
 * {@code ConditionEvaluationService}.
 * <p>
 * The block-only counterpart of {@link CantAttackUnlessEffect} (attack only) and the block half of
 * {@link CantAttackOrBlockUnlessEffect} (both). Example: Marauding Boneslasher —
 * {@code ControlsOtherPermanentCount(1, Zombie)} ("unless you control another Zombie").
 *
 * @param condition              the condition that must be met for this creature to block
 * @param requirementDescription human-readable "unless" clause (e.g. "you control another Zombie")
 */
public record CantBlockUnlessEffect(Condition condition, String requirementDescription)
        implements BlockingRestrictionEffect {

    @Override
    public Condition cantBlockUnless() {
        return condition;
    }
}
