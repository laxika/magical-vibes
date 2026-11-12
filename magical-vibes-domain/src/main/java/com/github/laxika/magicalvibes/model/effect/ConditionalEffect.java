package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;

/**
 * A generic conditional wrapper around a {@link CardEffect} that only applies when the
 * given {@link Condition} is met as the effect resolves (e.g. metalcraft, equipped, morbid, "if you do").
 * <p>
 * Targeting delegates to the wrapped effect so target selection works at cast time.
 * Condition evaluation is handled externally by the engine's
 * {@code ConditionEvaluationService} since domain records cannot depend on game services.
 * <p>
 * It is the "if" of an effect only. A triggered ability's intervening "if" ("When/Whenever/At [trigger event], if
 * [condition], [effect]", CR 603.4), checked as the ability triggers and again as it resolves, and a condition of
 * its trigger event (CR 603.2), checked only as it triggers, are the ability's
 * {@link com.github.laxika.magicalvibes.model.trigger.TriggerOptions}, not this wrapper: an "if" anywhere else in a
 * card's text has its normal meaning, so an ability with one triggers and its condition is judged as it resolves.
 * The engine wraps the effects of an ability with an intervening "if" in it as the ability goes on the stack, so
 * the condition is checked again as the ability resolves.
 */
public record ConditionalEffect(Condition condition, CardEffect wrapped)
        implements CardEffect {

    /** Human-readable condition name for log messages (e.g. "metalcraft", "equipped"). */
    public String conditionName() {
        return condition.conditionName();
    }

    /** Human-readable reason shown when the condition is not met (e.g. "fewer than three artifacts"). */
    public String conditionNotMetReason() {
        return condition.conditionNotMetReason();
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return condition instanceof NthAbilityResolutionThisTurn
                || wrapped.hasAbilityResolutionCondition();
    }

    /** A conditional static ability functions where the ability it conditions does. */
    @Override
    public AbilityZones staticAbilityZones() {
        return wrapped.staticAbilityZones();
    }

    @Override
    public GraveyardCardChoosingEffect graveyardCardChoice() {
        return wrapped.graveyardCardChoice();
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return wrapped.resolvesWhenTargetIllegal();
    }

    /** The reflexive ability the wrapped effect makes chooses its targets (Aurora Shifter's "if you do"). */
    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return wrapped.targetsChosenByReflexiveAbility();
    }

    /** The wrapped effect's player target relation ("target opponent"), under the intervening "if" around it. */
    @Override
    public com.github.laxika.magicalvibes.model.filter.PlayerRelation targetPlayerRelation() {
        return wrapped.targetPlayerRelation();
    }

    @Override
    public boolean targetChosenByActivePlayer() {
        return wrapped.targetChosenByActivePlayer();
    }

    @Override
    public boolean targetChosenAtRandom() {
        return wrapped.targetChosenAtRandom();
    }

    @Override
    public boolean hasOptionalTarget() {
        return wrapped.hasOptionalTarget();
    }
}
