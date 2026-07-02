package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * A generic conditional wrapper that selects between two effects based on a
 * {@link Condition}. When the condition is met the upgraded effect is resolved;
 * otherwise the base effect is used ("instead" patterns, e.g. kicker upgrades).
 * <p>
 * Targeting delegates to both inner effects so target selection works for either path.
 * Condition evaluation is handled externally by the engine's
 * {@code ConditionEvaluationService} since domain records cannot depend on game services.
 */
public record ConditionalReplacementEffect(
        Condition condition,
        CardEffect baseEffect,
        CardEffect upgradedEffect
) implements CardEffect {

    /** Human-readable condition name for log messages (e.g. "metalcraft"). */
    public String conditionName() {
        return condition.conditionName();
    }

    @Override
    public boolean canTargetPlayer() {
        return baseEffect.canTargetPlayer() || upgradedEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return baseEffect.canTargetPermanent() || upgradedEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return baseEffect.canTargetSpell() || upgradedEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return baseEffect.canTargetGraveyard() || upgradedEffect.canTargetGraveyard();
    }

    @Override
    public boolean isDamageOrDestruction() {
        return baseEffect.isDamageOrDestruction() || upgradedEffect.isDamageOrDestruction();
    }
}
