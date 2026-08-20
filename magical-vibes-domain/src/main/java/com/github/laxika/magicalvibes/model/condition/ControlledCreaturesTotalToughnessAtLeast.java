package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the total toughness of creatures controlled by the condition's controller is at
 * least {@code threshold}.
 */
public record ControlledCreaturesTotalToughnessAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "creatures you control have total toughness " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "creatures you control have total toughness less than " + threshold;
    }
}
