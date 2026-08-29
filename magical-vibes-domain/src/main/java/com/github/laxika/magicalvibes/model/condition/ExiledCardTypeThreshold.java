package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent has at least {@code threshold} distinct card types among cards exiled
 * with it.
 */
public record ExiledCardTypeThreshold(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "exiled card type threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " card types among cards exiled with it";
    }
}
