package com.github.laxika.magicalvibes.model.condition;

/** True when the damaged opponent meets Creepy Crawler's "afraid of you" condition. */
public record TargetPlayerIsAfraidOfController() implements Condition {

    @Override
    public String conditionName() {
        return "the targeted opponent is afraid of you";
    }

    @Override
    public String conditionNotMetReason() {
        return "the targeted opponent is not afraid of you";
    }
}
