package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Resolves several {@link CardEffect} steps in order, as if they were consecutive effects on the
 * same stack entry. The engine ({@code EffectResolutionService}) splices the steps into the entry's
 * live effect list when this effect is reached, so each step resolves through the ordinary loop —
 * pause/resume for async player input and nested wrappers work unchanged.
 *
 * <p><b>Semantics.</b> Steps resolve strictly in order with <em>no data flow between them</em>: there
 * is NO "if you do" contingency between steps. A step that no-ops (its target is gone, its condition
 * is unmet) does not stop the later steps — they still resolve. This is exactly how a flat list of
 * effects on one spell slot behaves, not the gated "if you do the first, then the second" of a
 * {@code MayEffect}-style contingency. An "if you do" is its own step that reads what an earlier step recorded
 * on the resolution scratchpad, e.g. {@code SequenceEffect.of(new SacrificeSelfEffect(),
 * new ConditionalEffect(PermanentLeftThisWay.sacrificed(), new DrawCardEffect(1)))} (Dreamcatcher); a step
 * that ends on a pending departure holds the later steps until the departure is done.</p>
 *
 * <p><b>When to use.</b> Only when a single-effect wrapper must gate several steps as one unit
 * (e.g. {@code MayEffect(SequenceEffect.of(a, b))}, {@code ConditionalEffect(cond, SequenceEffect.of(a, b))},
 * {@code ClashEffect(SequenceEffect.of(a, b))}, {@code FlipCoinWinEffect(SequenceEffect.of(a, b))}), or when
 * several steps must stay a single atomic triggered ability on one trigger slot (trigger collectors push one
 * stack entry per slot effect, so a multi-step bundle on a trigger slot must be one effect). For a plain spell
 * or ability effect list, prefer flat {@code addEffect(...)} calls (the Act of Treason / Drain Life pattern) —
 * do NOT wrap them in a sequence. Every separate effect is its own damage event, so flat slot effects that deal
 * damage at the same time ("… and 2 damage to you") are added with {@code Card.addSimultaneousEffects}; inside a
 * wrapper or an activated ability's effect list, use {@link #simultaneous} instead.</p>
 *
 * <p><b>Two dispatch paths.</b> Most wrappers ({@code MayEffect}, {@code ConditionalEffect}, trigger slots)
 * route their child back through {@code EffectResolutionService}'s loop, so the splice above applies and steps
 * may pause/resume for async player input. {@code ClashEffect} and {@code FlipCoinWinEffect} instead dispatch
 * their win/branch reward <em>synchronously</em> (via each step's own handler, not the loop), so a
 * {@code SequenceEffect} used there must contain only synchronous steps (no async player-input pause between
 * steps). Sentry Oak's {@code ClashEffect(SequenceEffect.of(BoostSelfEffect, RemoveKeywordEffect))} is the
 * canonical example — both steps resolve without interaction.</p>
 *
 * <p><b>Targeting.</b> {@link #targetSpec()} returns the first step with an explicitly declared
 * target, so at cast time the entry selects a single target for the sequence exactly as multiple
 * flat targeting effects on one slot share the entry's one target. An implicit source binding such
 * as {@link SacrificeSelfEffect} does not consume a target slot. A sequence bound to a target group
 * registers its spliced steps with that group; other target groups may be read by position.</p>
 *
 * <p>When used in an end-step trigger, the active end-step player is also carried in the stack
 * entry's {@code targetId}, allowing steps such as {@link DrawCardForTargetPlayerEffect} to act on
 * "that player".</p>
 */
public record SequenceEffect(List<CardEffect> steps, boolean optionalTarget, boolean simultaneous)
        implements CardEffect {

    public SequenceEffect(List<CardEffect> steps) {
        this(steps, false, false);
    }

    public SequenceEffect {
        steps = List.copyOf(steps);
        if (steps.size() < 2) {
            throw new IllegalArgumentException("SequenceEffect requires at least two steps, got " + steps.size());
        }
    }

    /** Convenience factory: {@code SequenceEffect.of(stepA, stepB, ...)}. */
    public static SequenceEffect of(CardEffect... steps) {
        return new SequenceEffect(List.of(steps));
    }

    /**
     * Creates a sequence whose steps happen at the same time ("deals 4 damage to any target and 2
     * damage to you"). The steps still resolve one after another, but the damage they deal is one
     * damage event: a lifelink source gains life once for all of it, and a "whenever this deals
     * damage" watcher sees one total. Steps of a plain sequence, like separate effects, are separate
     * events ("… Then …", or a second sentence).
     */
    public static SequenceEffect simultaneous(CardEffect... steps) {
        return new SequenceEffect(List.of(steps), false, true);
    }

    /** Returns this sequence with its steps replaced, keeping every other setting. */
    public SequenceEffect withSteps(List<CardEffect> newSteps) {
        return new SequenceEffect(newSteps, optionalTarget, simultaneous);
    }

    /** Creates a sequence whose single declared target may be omitted. */
    public static SequenceEffect upToOneTarget(CardEffect... steps) {
        return new SequenceEffect(List.of(steps), true, false);
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return steps.stream().anyMatch(CardEffect::hasAbilityResolutionCondition);
    }

    /**
     * Returns the first explicitly declared target in the sequence. Implicit source bindings such
     * as {@link SacrificeSelfEffect} do not consume a target slot; they are retained only when the
     * sequence has no explicit target at all.
     */
    @Override
    public TargetSpec targetSpec() {
        TargetSpec implicitSourceSpec = TargetSpec.NONE;
        for (CardEffect step : steps) {
            TargetSpec spec = step.targetSpec();
            if (spec.declaredTarget() != null) {
                return spec;
            }
            if (spec.selfTargeting()) {
                implicitSourceSpec = spec;
            }
        }
        return implicitSourceSpec;
    }

    @Override
    public boolean exertsAsItAttacks() {
        return steps.stream().anyMatch(CardEffect::exertsAsItAttacks);
    }

    @Override
    public boolean hasOptionalTarget() {
        if (optionalTarget) return true;
        for (CardEffect step : steps) {
            if (step.targetSpec().declaredTarget() != null) {
                return step.hasOptionalTarget();
            }
        }
        return false;
    }
}
