package com.github.laxika.magicalvibes.model.condition;

/** True when the referenced attacking creature is the only creature attacking that player. */
public record AttacksPlayerAlone() implements Condition {

    @Override
    public String conditionName() {
        return "attacks a player alone";
    }

    @Override
    public String conditionNotMetReason() {
        return "another creature is attacking that player";
    }
}
