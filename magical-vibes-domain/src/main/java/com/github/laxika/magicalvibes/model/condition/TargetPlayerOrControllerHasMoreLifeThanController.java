package com.github.laxika.magicalvibes.model.condition;

/** Compares the targeted player's life, or the targeted permanent's controller's life, to yours. */
public record TargetPlayerOrControllerHasMoreLifeThanController() implements Condition {

    @Override
    public String conditionName() {
        return "that player or that permanent's controller has more life than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player or that permanent's controller does not have more life than you";
    }
}
