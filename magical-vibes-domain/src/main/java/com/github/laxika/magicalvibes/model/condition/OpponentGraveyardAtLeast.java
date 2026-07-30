package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent of the controller has at least {@code threshold} cards in their graveyard
 * (Jace's Phantasm). Unlike {@link AnyGraveyardAtLeast} the controller's own graveyard
 * never satisfies this condition.
 */
public record OpponentGraveyardAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent has " + threshold + " or more cards in their graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has " + threshold + " or more cards in their graveyard";
    }
}
