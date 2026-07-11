package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the total power of creatures the controller controls is at least {@code threshold}
 * (Mosswort Bridge's "if creatures you control have total power 10 or greater").
 */
public record ControlledCreaturesTotalPowerAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "creatures you control have total power " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "creatures you control have total power less than " + threshold;
    }
}
