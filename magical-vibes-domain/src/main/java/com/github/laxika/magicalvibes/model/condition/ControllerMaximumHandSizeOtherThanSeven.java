package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller's effective maximum hand size is not seven, including when that player has no
 * maximum hand size.
 */
public record ControllerMaximumHandSizeOtherThanSeven() implements Condition {

    @Override
    public String conditionName() {
        return "a maximum hand size other than 7";
    }

    @Override
    public String conditionNotMetReason() {
        return "your maximum hand size is 7";
    }
}
