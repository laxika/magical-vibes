package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has at least {@code minimum} Equipment attached to it. */
public record SourceHasAtLeastAttachedEquipment(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "source has at least " + minimum + " Equipment attached";
    }

    @Override
    public String conditionNotMetReason() {
        return "source has fewer than " + minimum + " Equipment attached";
    }
}
