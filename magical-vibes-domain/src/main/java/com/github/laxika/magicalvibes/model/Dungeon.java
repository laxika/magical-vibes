package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Dungeon {

    LOST_MINE_OF_PHANDELVER(4),
    TOMB_OF_ANNIHILATION(4),
    DUNGEON_OF_THE_MAD_MAGE(7);

    private final int roomCount;
}
