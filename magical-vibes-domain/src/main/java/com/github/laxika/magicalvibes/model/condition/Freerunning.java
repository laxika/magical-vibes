package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller dealt combat damage to a player this turn with an Assassin or commander.
 */
public record Freerunning() implements Condition {

    @Override
    public String conditionName() {
        return "freerunning";
    }

    @Override
    public String conditionNotMetReason() {
        return "you did not deal combat damage to a player this turn with an Assassin or commander";
    }
}
