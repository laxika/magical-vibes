package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least {@code threshold} distinct colors among permanents they control. */
public record ColorsAmongControlledPermanentsAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "at least " + threshold + " colors among controlled permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " colors among controlled permanents";
    }
}
