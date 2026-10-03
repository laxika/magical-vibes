package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Dungeon {

    LOST_MINE_OF_PHANDELVER(7),
    TOMB_OF_ANNIHILATION(5),
    DUNGEON_OF_THE_MAD_MAGE(9),
    UNDERCITY(9);

    private final int roomCount;

    /** Outgoing arrows from the marked room; values identify the rooms, rather than rows. */
    public java.util.List<Integer> nextRooms(int roomIndex) {
        return switch (this) {
            case LOST_MINE_OF_PHANDELVER -> switch (roomIndex) {
                case 0 -> java.util.List.of(1, 2);
                case 1 -> java.util.List.of(3, 4);
                case 2 -> java.util.List.of(4, 5);
                case 3, 4, 5 -> java.util.List.of(6);
                default -> java.util.List.of();
            };
            case TOMB_OF_ANNIHILATION -> switch (roomIndex) {
                case 0 -> java.util.List.of(1, 3);
                case 1 -> java.util.List.of(2);
                case 2, 3 -> java.util.List.of(4);
                default -> java.util.List.of();
            };
            case DUNGEON_OF_THE_MAD_MAGE -> switch (roomIndex) {
                case 0 -> java.util.List.of(1);
                case 1 -> java.util.List.of(2, 3);
                case 2, 3 -> java.util.List.of(4);
                case 4 -> java.util.List.of(5, 6);
                case 5, 6 -> java.util.List.of(7);
                case 7 -> java.util.List.of(8);
                default -> java.util.List.of();
            };
            case UNDERCITY -> switch (roomIndex) {
                case 0 -> java.util.List.of(1, 2);
                case 1 -> java.util.List.of(3, 4);
                case 2 -> java.util.List.of(4, 5);
                case 3, 4 -> java.util.List.of(6, 7);
                case 5 -> java.util.List.of(7);
                case 6, 7 -> java.util.List.of(8);
                default -> java.util.List.of();
            };
        };
    }
}
