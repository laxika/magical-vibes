package com.github.laxika.magicalvibes.model.condition;

/**
 * At least one opponent has strictly more life than the controller
 * (e.g. Timely Reinforcements: "if you have less life than an opponent").
 */
public record AnOpponentHasMoreLifeThanController() implements Condition {

    @Override
    public String conditionName() {
        return "less life than an opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has more life than you";
    }
}
