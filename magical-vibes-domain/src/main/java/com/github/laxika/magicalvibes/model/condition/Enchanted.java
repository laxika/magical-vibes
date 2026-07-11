package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has at least one Aura attached (it is enchanted). */
public record Enchanted() implements Condition {

    @Override
    public String conditionName() {
        return "enchanted";
    }

    @Override
    public String conditionNotMetReason() {
        return "not enchanted";
    }
}
