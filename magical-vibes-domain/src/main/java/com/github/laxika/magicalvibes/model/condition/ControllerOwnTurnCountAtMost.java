package com.github.laxika.magicalvibes.model.condition;

/**
 * It is the controller's turn and the number of turns the controller has taken this game
 * (counting the turn in progress and any extra turns) is at most {@code maxTurns}.
 *
 * <p>Wrap in {@link NotCondition} for the "You can't cast this during your first, second, or third
 * turns of the game" cast restriction (Serra Avenger uses {@code maxTurns = 3}). Because the clause
 * only speaks of the controller's own turns, the condition is false during an opponent's turn, so a
 * negated form correctly permits casting at instant speed on an opponent's turn.
 */
public record ControllerOwnTurnCountAtMost(int maxTurns) implements Condition {

    @Override
    public String conditionName() {
        return "controller's turn and they have taken at most " + maxTurns + " turns";
    }

    @Override
    public String conditionNotMetReason() {
        return "not one of the controller's first " + maxTurns + " turns";
    }
}
