package com.github.laxika.magicalvibes.model.condition;

/** No player is currently the monarch. */
public record NoMonarch() implements Condition {

    @Override
    public String conditionName() {
        return "there is no monarch";
    }

    @Override
    public String conditionNotMetReason() {
        return "a player is already the monarch";
    }
}
