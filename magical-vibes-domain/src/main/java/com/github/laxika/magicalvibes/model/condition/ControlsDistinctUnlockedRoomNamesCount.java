package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least {@code minCount} different names among unlocked Room doors. */
public record ControlsDistinctUnlockedRoomNamesCount(int minCount) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more different names among unlocked Room doors";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount
                + " different names among unlocked Room doors";
    }
}
