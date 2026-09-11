package com.github.laxika.magicalvibes.model.condition;

/** The source Room permanent has the given door unlocked. */
public record SourceRoomDoorUnlocked(int doorIndex) implements Condition {

    public SourceRoomDoorUnlocked {
        if (doorIndex < 0 || doorIndex > 1) {
            throw new IllegalArgumentException("Room door index must be 0 or 1");
        }
    }

    @Override
    public String conditionName() {
        return "Room door " + doorIndex + " unlocked";
    }

    @Override
    public String conditionNotMetReason() {
        return "Room door " + doorIndex + " is locked";
    }
}
