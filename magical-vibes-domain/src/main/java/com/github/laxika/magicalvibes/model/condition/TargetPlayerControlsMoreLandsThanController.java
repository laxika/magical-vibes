package com.github.laxika.magicalvibes.model.condition;

/** True when the targeted player is an opponent who controls more lands than the controller. */
public record TargetPlayerControlsMoreLandsThanController() implements Condition {

    @Override
    public String conditionName() {
        return "that player controls more lands than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player does not control more lands than you";
    }
}
