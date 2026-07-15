package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The declarative targeting descriptor for a {@link CardEffect}: one immutable value that
 * replaces the eleven legacy per-effect targeting methods on {@code CardEffect}.
 *
 * <p>An effect exposes its targeting through the derived interface method
 * {@code CardEffect.targetSpec()} — NEVER as a record component. Effects are Jackson-serialized
 * by their record components, so adding a component would change the wire format and
 * {@code equals()}; {@code targetSpec()} is computed from the effect's existing components
 * instead.</p>
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
}
