package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has cards with at least {@code threshold} different mana values exiled with it. */
public record SourceExiledDifferentManaValuesThreshold(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "source-exiled different-mana-value threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " different mana values exiled with source";
    }
}
