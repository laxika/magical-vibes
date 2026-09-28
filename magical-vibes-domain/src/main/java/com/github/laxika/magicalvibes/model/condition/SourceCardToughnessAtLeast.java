package com.github.laxika.magicalvibes.model.condition;

/** True when the source card's current toughness, including perpetual modifiers, is at least the threshold. */
public record SourceCardToughnessAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "source card's toughness is " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "the source card's toughness is less than " + threshold;
    }
}
