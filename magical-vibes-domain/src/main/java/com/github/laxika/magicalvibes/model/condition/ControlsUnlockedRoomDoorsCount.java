package com.github.laxika.magicalvibes.model.condition;

/** The controller has at least {@code minCount} unlocked doors among Rooms they control. */
public record ControlsUnlockedRoomDoorsCount(int minCount) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more unlocked Room doors";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " unlocked Room doors";
    }
}
