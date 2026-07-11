package com.github.laxika.magicalvibes.model.condition;

/** The source creature attacks alone (CR 506.5): it is the only declared attacker. */
public record AttacksAlone() implements Condition {

    @Override
    public String conditionName() {
        return "attacks alone";
    }

    @Override
    public String conditionNotMetReason() {
        return "creature did not attack alone";
    }
}
