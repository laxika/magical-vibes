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

    /**
     * Convenience form for "when the condition is met, apply this effect; otherwise no
     * replacement happens." Used by the conditional enters-tapped land cycles (check lands,
     * fast lands, slow lands): the condition is the negated unless-clause and the upgraded
     * effect is {@link EntersTappedEffect}. {@code baseEffect} is {@code null} — the entering
     * permanent is simply left untapped — so this form is only meaningful for replacement
     * effects processed during entry, never for stack resolution.
     */
    public ConditionalReplacementEffect(Condition condition, CardEffect upgradedEffect) {
        this(condition, null, upgradedEffect);
    }

    /** Human-readable condition name for log messages (e.g. "metalcraft"). */
    public String conditionName() {
        return condition.conditionName();
    }

    @Override
    public boolean canTargetPlayer() {
        return (baseEffect != null && baseEffect.canTargetPlayer()) || upgradedEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return (baseEffect != null && baseEffect.canTargetPermanent()) || upgradedEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return (baseEffect != null && baseEffect.canTargetSpell()) || upgradedEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return (baseEffect != null && baseEffect.canTargetGraveyard()) || upgradedEffect.canTargetGraveyard();
    }

    @Override
    public boolean isDamageOrDestruction() {
        return (baseEffect != null && baseEffect.isDamageOrDestruction()) || upgradedEffect.isDamageOrDestruction();
    }

    @Override
    public boolean isSelfTargeting() {
        return (baseEffect != null && baseEffect.isSelfTargeting()) || upgradedEffect.isSelfTargeting();
    }
}
