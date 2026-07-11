package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has strictly more life than at least one opponent
 * (e.g. Feudkiller's Verdict: "if you have more life than an opponent").
 */
public record ControllerHasMoreLifeThanAnOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "more life than an opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has less life than you";
    }
}
