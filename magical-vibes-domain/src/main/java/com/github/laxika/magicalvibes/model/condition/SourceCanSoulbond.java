package com.github.laxika.magicalvibes.model.condition;

/**
 * Soulbond self-ETB intervening-if (CR 702.94a): the source is unpaired and the controller
 * controls at least one other unpaired creature.
 */
public record SourceCanSoulbond() implements Condition {

    @Override
    public String conditionName() {
        return "soulbond";
    }

    @Override
    public String conditionNotMetReason() {
        return "no unpaired creature to pair with";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
