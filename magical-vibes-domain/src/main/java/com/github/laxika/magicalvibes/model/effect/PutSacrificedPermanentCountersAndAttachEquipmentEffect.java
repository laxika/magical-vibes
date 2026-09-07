package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the counters copied from a sacrificed source permanent onto the target creature and
 * attaches one Equipment that was attached to that source when it was sacrificed.
 */
public record PutSacrificedPermanentCountersAndAttachEquipmentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
