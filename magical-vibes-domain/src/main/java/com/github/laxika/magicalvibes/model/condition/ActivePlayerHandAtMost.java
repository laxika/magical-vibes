package com.github.laxika.magicalvibes.model.condition;

/**
 * The active player (the player whose turn/step it is) has {@code threshold} or fewer cards in
 * hand. The small-hand counterpart of {@link ActivePlayerHandAtLeast} for upkeep triggers — "if
 * that player has one or fewer cards in hand" during each opponent's upkeep resolves to the active
 * player (Shrieking Affliction).
 */
public record ActivePlayerHandAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return threshold + " or fewer cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "the player has more than " + threshold + " cards in hand";
    }
}
