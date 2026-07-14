package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Static combat restriction: this creature can't attack or block unless the given {@link Condition}
 * is met. The attack side is evaluated at attack-declaration legality time in the combat service and
 * the block side in {@code GameQueryService.canBlock}, both via {@code ConditionEvaluationService}.
 * <p>
 * The condition/block-restriction counterpart of {@link CantAttackUnlessEffect} (attack only).
 * Example: Blind-Spot Giant — {@code ControlsAnotherPermanent(Giant)} ("unless you control another Giant").
 *
 * @param condition              the condition that must be met for this creature to attack or block
 * @param requirementDescription human-readable "unless" clause (e.g. "you control another Giant")
 */
public record CantAttackOrBlockUnlessEffect(Condition condition, String requirementDescription)
        implements AttackOrBlockRestrictionEffect {

    @Override
    public Condition cantAttackOrBlockUnless() {
        return condition;
    }

    @Override
    public String restrictionDescription() {
        return requirementDescription;
    }
}
