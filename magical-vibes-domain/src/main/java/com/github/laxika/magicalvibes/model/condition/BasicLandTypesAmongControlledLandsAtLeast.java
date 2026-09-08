package com.github.laxika.magicalvibes.model.condition;

/** A controller has at least the specified number of distinct basic land types among their lands. */
public record BasicLandTypesAmongControlledLandsAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return threshold + "+ basic land types among lands you control";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " basic land types among lands you control";
    }
}
