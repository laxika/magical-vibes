package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least {@code threshold} distinct card types among their permanents and graveyard. */
public record CardTypesAmongControlledPermanentsAndGraveyardAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "at least " + threshold + " card types among controlled permanents and graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " card types among controlled permanents and graveyard";
    }
}
