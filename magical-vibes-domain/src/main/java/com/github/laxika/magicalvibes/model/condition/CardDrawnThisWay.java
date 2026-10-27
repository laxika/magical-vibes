package com.github.laxika.magicalvibes.model.condition;

/**
 * "If you do" after a draw: the most recent draw step of the resolving entry actually drew a card, as
 * recorded in its resolution scratchpad ({@code StackEntry.getResolutionResults()}). A draw that was
 * prevented or replaced draws nothing, so the condition fails. Use it with {@code ConditionalEffect.unless},
 * since it is only meaningful as the entry resolves.
 */
public record CardDrawnThisWay() implements Condition {

    @Override
    public String conditionName() {
        return "drew a card this way";
    }

    @Override
    public String conditionNotMetReason() {
        return "no card was drawn";
    }
}
