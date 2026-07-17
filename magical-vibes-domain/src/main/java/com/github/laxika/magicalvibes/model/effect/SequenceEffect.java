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
 * {@code MayEffect}-style contingency.</p>
 *
 * <p><b>When to use.</b> Only when a single-effect wrapper must gate several steps as one unit
 * (e.g. {@code MayEffect(SequenceEffect.of(a, b))}, {@code ConditionalEffect(cond, SequenceEffect.of(a, b))},
 * {@code FlipCoinWinEffect(SequenceEffect.of(a, b))}), or when several steps must stay a single atomic
 * triggered ability on one trigger slot (trigger collectors push one stack entry per slot effect, so
 * a multi-step bundle on a trigger slot must be one effect). For a plain spell or ability effect list,
 * prefer flat {@code addEffect(...)} calls (the Act of Treason / Drain Life pattern) — do NOT wrap
 * them in a sequence.</p>
 *
 * <p><b>Targeting.</b> {@link #targetSpec()} returns the first step's non-{@link TargetSpec#NONE}
 * spec, so at cast time the entry selects a single target for the sequence exactly as multiple flat
 * targeting effects on one slot share the entry's one target. Multi-target groups inside a sequence
 * are <em>unsupported</em>: the spliced steps are not registered in the card's effect→target-group
 * table, so every targeting step reads the entry's shared {@code targetId}. Use flat, group-bound
 * effects on the card for genuinely multi-target abilities.</p>
 */
public record SequenceEffect(List<CardEffect> steps) implements CardEffect {

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

    @Override
    public TargetSpec targetSpec() {
        for (CardEffect step : steps) {
            TargetSpec spec = step.targetSpec();
            if (spec != TargetSpec.NONE) {
                return spec;
            }
        }
        return TargetSpec.NONE;
    }
}
