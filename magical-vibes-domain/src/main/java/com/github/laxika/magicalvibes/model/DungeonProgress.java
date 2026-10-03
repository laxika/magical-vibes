package com.github.laxika.magicalvibes.model;

/** The dungeon and zero-based room currently marked by a player's venture marker. */
public record DungeonProgress(Dungeon dungeon, int roomIndex) {

    public DungeonProgress {
        if (roomIndex < 0 || roomIndex >= dungeon.getRoomCount()) {
            throw new IllegalArgumentException("Room index is outside the dungeon");
        }
    }

    public boolean isBottomRoom() {
        return roomIndex == dungeon.getRoomCount() - 1;
    }

    public DungeonProgress advance() {
        if (isBottomRoom()) {
            throw new IllegalStateException("Cannot advance beyond the bottom room");
        }
        java.util.List<Integer> choices = dungeon.nextRooms(roomIndex);
        if (choices.size() != 1) {
            throw new IllegalStateException("The controller must choose the next dungeon room");
        }
        return new DungeonProgress(dungeon, choices.getFirst());
    }
}
