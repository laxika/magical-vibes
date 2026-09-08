package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least {@code threshold} distinct card types among cards in their graveyard. */
public record CardTypesAmongCardsInGraveyardAtLeast(int threshold) implements Condition {

    public CardTypesAmongCardsInGraveyardAtLeast {
        if (threshold < 0) {
            throw new IllegalArgumentException("Card type threshold cannot be negative");
        }
    }

    @Override
    public String conditionName() {
        return "at least " + threshold + " card types among cards in graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " card types among cards in graveyard";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
