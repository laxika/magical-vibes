package com.github.laxika.magicalvibes.model.condition;

/** True when one of the source permanent's opponents is the monarch. */
public record AnOpponentIsMonarch() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent is the monarch";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent is the monarch";
    }
}
