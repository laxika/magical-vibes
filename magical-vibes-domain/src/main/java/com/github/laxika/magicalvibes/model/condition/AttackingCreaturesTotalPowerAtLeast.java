package com.github.laxika.magicalvibes.model.condition;

/** True when the controller's currently attacking creatures have total power at least the threshold. */
public record AttackingCreaturesTotalPowerAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "attacking creatures have total power " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "attacking creatures have total power less than " + threshold;
    }
}
