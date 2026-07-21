package com.github.laxika.magicalvibes.model.condition;

/**
 * True while the source permanent is soulbond-paired with another creature (CR 702.94).
 */
public record SourceIsPaired() implements Condition {

    @Override
    public String conditionName() {
        return "paired";
    }

    @Override
    public String conditionNotMetReason() {
        return "not paired with another creature";
    }
}
