package com.github.laxika.magicalvibes.model.condition;

/** The controller has the city's blessing. */
public record ControllerHasCityBlessing() implements Condition {

    @Override
    public String conditionName() {
        return "the city's blessing";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller does not have the city's blessing";
    }
}
