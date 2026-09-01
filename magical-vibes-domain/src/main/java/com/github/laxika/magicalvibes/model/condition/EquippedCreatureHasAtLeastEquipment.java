package com.github.laxika.magicalvibes.model.condition;

/** The creature the source Equipment is attached to has at least {@code minimum} Equipment attached. */
public record EquippedCreatureHasAtLeastEquipment(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "equipped creature has at least " + minimum + " Equipment attached";
    }

    @Override
    public String conditionNotMetReason() {
        return "equipped creature has fewer than " + minimum + " Equipment attached";
    }
}
