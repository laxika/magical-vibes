package com.github.laxika.magicalvibes.model.condition;

/** The source creature is being blocked by at least {@code minBlockers} creatures. */
public record BlockedByMinCreatures(int minBlockers) implements Condition {

    @Override
    public String conditionName() {
        return "blocked by " + minBlockers + "+ creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minBlockers + " creatures blocking";
    }
}
