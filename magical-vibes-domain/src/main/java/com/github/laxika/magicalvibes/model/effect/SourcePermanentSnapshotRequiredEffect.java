package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a triggered effect whose source state must be snapshotted when the trigger is created.
 */
public interface SourcePermanentSnapshotRequiredEffect extends CardEffect {

    /**
     * Whether {@code effect}, or an effect nested in a {@link ConditionalEffect} or
     * {@link SequenceEffect}, needs the source permanent snapshotted when its trigger is created.
     */
    static boolean isRequiredBy(CardEffect effect) {
        if (effect instanceof SourcePermanentSnapshotRequiredEffect) {
            return true;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return isRequiredBy(conditional.wrapped());
        }
        return effect instanceof SequenceEffect sequence
                && sequence.steps().stream().anyMatch(SourcePermanentSnapshotRequiredEffect::isRequiredBy);
    }
}
