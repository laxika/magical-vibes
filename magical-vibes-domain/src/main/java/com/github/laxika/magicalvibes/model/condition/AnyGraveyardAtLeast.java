package com.github.laxika.magicalvibes.model.condition;

/**
 * Some player's graveyard has at least {@code threshold} cards in it (Visions of Beyond).
 */
public record AnyGraveyardAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "a graveyard has " + threshold + " or more cards";
    }

    @Override
    public String conditionNotMetReason() {
        return "no graveyard has " + threshold + " or more cards";
    }
}
