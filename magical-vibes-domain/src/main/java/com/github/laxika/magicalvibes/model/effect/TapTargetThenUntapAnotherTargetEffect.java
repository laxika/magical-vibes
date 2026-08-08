package com.github.laxika.magicalvibes.model.effect;

/**
 * "Tap target permanent, then untap another target permanent." (Ral Zarek +1).
 *
 * <p>A two-position multi-target effect: the first chosen target is tapped, the second is untapped.
 * The two positions cannot be filled by the same permanent — the multi-target path already rejects
 * duplicate targets, which is exactly what "another" requires. Declare it on an ability with two
 * permanent filters and {@code minTargets = maxTargets = 2}; each target is looked up independently
 * at resolution, so one having left the battlefield does not stop the other half.
 */
public record TapTargetThenUntapAnotherTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        // The targets ride entry.getTargetIds() and are validated on the multi-target path, so the
        // spec stays a permissive permanent spec (mirrors UntapPermanentsEffect's ALL_TARGETS).
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
