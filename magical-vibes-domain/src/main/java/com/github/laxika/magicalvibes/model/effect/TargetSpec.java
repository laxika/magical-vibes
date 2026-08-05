package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The declarative targeting descriptor for a {@link CardEffect}: one immutable value that
 * replaces the eleven legacy per-effect targeting methods on {@code CardEffect}.
 *
 * <p>An effect exposes its targeting through the derived interface method
 * {@code CardEffect.targetSpec()} — NEVER as a record component. A component would change the
 * effect's {@code equals()}, which effect handlers and the AI compare on, and would have to be
 * threaded through every constructor of a ~400-record hierarchy; {@code targetSpec()} is computed
 * from the effect's existing components instead. (Serialization is not the reason: nothing
 * deserializes effects — {@code CardEffect} carries no {@code @JsonTypeInfo}, the wire protocol
 * sends resolved UUID lists and derived booleans, and {@code magical-vibes-networking} references
 * neither {@link TargetCategory} nor the predicate hierarchy.)</p>
 *
 * @param declaredTarget   which targets are legal, as a composable {@link TargetPredicate} built
 *                         from a {@link TargetPredicates} factory; {@code null} when the effect
 *                         targets nothing. This is the declaration <em>before</em> the orthogonal
 *                         {@link #predicate()} narrowing is folded in — read
 *                         {@link #targetPredicate()} for the full restriction.
 * @param harmful          {@code true} when the effect deals damage / destroys / exiles /
 *                         sacrifices / fights its target — i.e. protection from the source must
 *                         be honoured (mirrors the old {@code isDamageOrDestruction()} and the
 *                         validator's {@code checkProtection} call)
 * @param predicate        an optional narrowing predicate over a permanent target (artifact-only,
 *                         nonland, a subtype, …); {@code null} when the declared target alone
 *                         suffices. Kept as its own component because readers such as
 *                         {@code EffectResolution.targetPredicateOf} need the narrowing on its
 *                         own, not conjoined with the declared type restriction.
 * @param selfTargeting    {@code true} when the effect implicitly targets its own source
 *                         permanent (boost-self, regenerate-self, …)
 * @param playerTargetCount how many distinct player targets the effect requires (default 1)
 */
public record TargetSpec(
        TargetPredicate declaredTarget,
        boolean harmful,
        PermanentPredicate predicate,
        boolean selfTargeting,
        int playerTargetCount) {

    /** The spec for an effect that targets nothing — every derived legacy value is its default. */
    public static final TargetSpec NONE = new TargetSpec(null, false, null, false, 1);

    /** A harmful (protection-honouring) spec for the given target, no narrowing predicate. */
    public static TargetSpec harmful(TargetPredicate declaredTarget) {
        return new TargetSpec(declaredTarget, true, null, false, 1);
    }

    /** A benign (no protection check) spec for the given target, no narrowing predicate. */
    public static TargetSpec benign(TargetPredicate declaredTarget) {
        return new TargetSpec(declaredTarget, false, null, false, 1);
    }

    /** A harmful spec for the given target narrowed by the given permanent predicate. */
    public static TargetSpec harmful(TargetPredicate declaredTarget, PermanentPredicate predicate) {
        return new TargetSpec(declaredTarget, true, predicate, false, 1);
    }

    /** A benign spec for the given target narrowed by the given permanent predicate. */
    public static TargetSpec benign(TargetPredicate declaredTarget, PermanentPredicate predicate) {
        return new TargetSpec(declaredTarget, false, predicate, false, 1);
    }

    /**
     * The full restriction this spec places on a target: {@link #declaredTarget()} with its
     * permanent leaf additionally narrowed by {@link #predicate()}, or {@code null} when the spec
     * targets nothing.
     */
    public TargetPredicate targetPredicate() {
        return TargetPredicates.narrowPermanents(declaredTarget, predicate);
    }

    /**
     * The legacy {@link TargetCategory} equivalent of {@link #declaredTarget()}.
     *
     * <p>Transitional bridge for the readers that still switch on the enum (trigger collectors,
     * {@code StepTriggerService}, the AI, {@code EffectResolution.collectTargetTypes}); it is
     * deleted together with {@code TargetCategory} — see
     * {@code agent-docs/TARGET_PREDICATE_PLAN.md}.</p>
     */
    public TargetCategory category() {
        return TargetPredicates.categoryOf(declaredTarget);
    }

    /**
     * Whether a target of {@code kind} can ever be legal for this spec — the null-safe form of
     * {@link TargetPredicate#admits(TargetPredicate.Kind)}, since a spec that targets nothing has
     * no predicate at all.
     */
    public boolean admits(TargetPredicate.Kind kind) {
        TargetPredicate targetPredicate = targetPredicate();
        return targetPredicate != null && targetPredicate.admits(kind);
    }
}
