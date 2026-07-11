package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * A generic conditional wrapper around a {@link CardEffect} that only applies when the
 * given {@link Condition} is met (e.g. metalcraft, equipped, morbid).
 * <p>
 * Targeting delegates to the wrapped effect so target selection works at cast time.
 * Condition evaluation is handled externally by the engine's
 * {@code ConditionEvaluationService} since domain records cannot depend on game services.
 */
public record ConditionalEffect(Condition condition, CardEffect wrapped) implements CardEffect {

    /** Human-readable condition name for log messages (e.g. "metalcraft", "equipped"). */
    public String conditionName() {
        return condition.conditionName();
    }

    /** Human-readable reason shown when the condition is not met (e.g. "fewer than three artifacts"). */
    public String conditionNotMetReason() {
        return condition.conditionNotMetReason();
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }

    @Override
    public boolean isDamageOrDestruction() {
        return wrapped.isDamageOrDestruction();
    }

    @Override
    public boolean isSelfTargeting() {
        return wrapped.isSelfTargeting();
    }
}
