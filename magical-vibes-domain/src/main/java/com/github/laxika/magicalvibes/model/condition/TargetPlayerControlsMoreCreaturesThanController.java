package com.github.laxika.magicalvibes.model.condition;

/** True when the targeted player controls strictly more creatures than the condition's controller. */
public record TargetPlayerControlsMoreCreaturesThanController() implements Condition {

    @Override
    public String conditionName() {
        return "that player controls more creatures than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player does not control more creatures than you";
    }
}
