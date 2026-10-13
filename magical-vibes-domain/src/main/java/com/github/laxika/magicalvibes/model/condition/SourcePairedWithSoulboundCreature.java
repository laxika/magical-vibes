package com.github.laxika.magicalvibes.model.condition;

/**
 * The source is paired with another creature that still has soulbond. Losing soulbond does not
 * end a pairing (CR 702.95e), so "paired with a creature with soulbond" needs the partner's
 * current keywords checked as well as the pairing itself. Flowering Lumberknot.
 */
public record SourcePairedWithSoulboundCreature() implements Condition {

    @Override
    public String conditionName() {
        return "paired with a creature with soulbond";
    }

    @Override
    public String conditionNotMetReason() {
        return "not paired with a creature with soulbond";
    }
}
