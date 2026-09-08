package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has at least {@code threshold} non-token cards exiled with it. */
public record SourceExiledCardsThreshold(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "source-exiled card threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " non-token cards exiled with source";
    }
}
