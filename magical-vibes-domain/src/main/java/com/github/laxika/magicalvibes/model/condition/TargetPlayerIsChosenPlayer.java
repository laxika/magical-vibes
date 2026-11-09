package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the player the context names is one the source permanent chose as it entered ("the chosen player" of
 * {@code ChooseOpponentOnEnterEffect}): the player the event was about, as a trigger's collector judges it. As a draw
 * trigger's event condition it is "when the chosen player draws a card" (Booby Trap). The chosen player stays the same
 * when control of the source changes; it matches no player when the source chose none or is gone.
 */
public record TargetPlayerIsChosenPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "that player is the chosen player";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player is not the chosen player";
    }
}
