package com.github.laxika.magicalvibes.model.condition;

/** The controller has cycled at least the specified number of cards with the given name this game. */
public record ControllerCycledCardNamedAtLeastThisGame(int minimum, String cardName) implements Condition {

    @Override
    public String conditionName() {
        return "you've cycled " + minimum + " or more cards named " + cardName + " this game";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cycled " + minimum + " cards named " + cardName + " this game";
    }
}
