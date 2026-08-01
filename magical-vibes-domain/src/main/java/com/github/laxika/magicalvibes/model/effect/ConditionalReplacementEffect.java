package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * A generic conditional wrapper that selects between two effects based on a
 * {@link Condition}. When the condition is met the upgraded effect is resolved;
 * otherwise the base effect is used ("instead" patterns, e.g. kicker upgrades).
 * <p>
 * Targeting delegates to the upgraded effect. Every base/upgraded pair is a magnitude-only
 * replacement (same target shape, e.g. "deal 3" vs "deal 5"), so the upgraded effect's
 * {@code targetSpec()} reproduces the base-OR-upgraded targeting exactly; the enters-tapped
 * convenience form has a {@code null} base and a non-targeting upgraded effect. Condition
 * evaluation is handled externally by the engine's {@code ConditionEvaluationService} since
 * domain records cannot depend on game services.
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
    public TargetSpec targetSpec() {
        // Overload is the one condition whose branches differ in target shape (CR 702.96b: an
        // overloaded spell has no targets at all), so the upgraded branch cannot describe the pair.
        // Report the printed, non-overloaded targeting here; a cast that actually pays the overload
        // cost resolves the wrapper away first via
        // EffectResolution.resolveEffects(effects, kicked, overloaded, modeIndex).
        if (condition instanceof com.github.laxika.magicalvibes.model.condition.Overloaded && baseEffect != null) {
            return baseEffect.targetSpec();
        }
        return upgradedEffect.targetSpec();
    }
}
