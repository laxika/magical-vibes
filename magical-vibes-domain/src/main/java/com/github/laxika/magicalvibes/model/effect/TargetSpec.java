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
 * @param category         which kind of target is legal (see {@link TargetCategory})
 * @param harmful          {@code true} when the effect deals damage / destroys / exiles /
 *                         sacrifices / fights its target — i.e. protection from the source must
 *                         be honoured (mirrors the old {@code isDamageOrDestruction()} and the
 *                         validator's {@code checkProtection} call)
 * @param predicate        an optional narrowing predicate over a permanent target (artifact-only,
 *                         nonland, a subtype, …); {@code null} when the category alone suffices
 * @param selfTargeting    {@code true} when the effect implicitly targets its own source
 *                         permanent (boost-self, regenerate-self, …)
 * @param playerTargetCount how many distinct player targets the effect requires (default 1)
 */
public record TargetSpec(
        TargetCategory category,
        boolean harmful,
        PermanentPredicate predicate,
        boolean selfTargeting,
        int playerTargetCount) {

    /** The spec for an effect that targets nothing — every derived legacy value is its default. */
    public static final TargetSpec NONE = new TargetSpec(TargetCategory.NONE, false, null, false, 1);

    /** A harmful (protection-honouring) spec for the given category, no predicate. */
    public static TargetSpec harmful(TargetCategory category) {
        return new TargetSpec(category, true, null, false, 1);
    }

    /** A benign (no protection check) spec for the given category, no predicate. */
    public static TargetSpec benign(TargetCategory category) {
        return new TargetSpec(category, false, null, false, 1);
    }

    /** A harmful spec for the given category narrowed by the given permanent predicate. */
    public static TargetSpec harmful(TargetCategory category, PermanentPredicate predicate) {
        return new TargetSpec(category, true, predicate, false, 1);
    }

    /** A benign spec for the given category narrowed by the given permanent predicate. */
    public static TargetSpec benign(TargetCategory category, PermanentPredicate predicate) {
        return new TargetSpec(category, false, predicate, false, 1);
    }

    /**
     * The composable equivalent of {@link #category()} plus {@link #predicate()}, or {@code null}
     * when the spec targets nothing.
     *
     * <p>Derived, not stored: {@code category} remains the source of truth and every call site
     * still reads it. This exists so the enum's two lossy derived booleans
     * ({@code includesPermanents()} / {@code includesPlayers()}) can be replaced one call site at
     * a time — see {@code agent-docs/TARGET_PREDICATE_PLAN.md}.</p>
     */
    public TargetPredicate targetPredicate() {
        return TargetPredicates.narrowPermanents(TargetPredicates.forCategory(category), predicate);
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
