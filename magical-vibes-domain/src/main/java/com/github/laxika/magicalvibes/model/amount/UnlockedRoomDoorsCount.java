package com.github.laxika.magicalvibes.model.amount;

/** The number of unlocked doors among Rooms controlled by the selected player scope. */
public record UnlockedRoomDoorsCount(CountScope scope) implements DynamicAmount {
}
