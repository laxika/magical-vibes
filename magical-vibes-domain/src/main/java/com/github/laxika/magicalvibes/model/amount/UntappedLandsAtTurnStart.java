package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of untapped lands the targeted player controlled at the beginning of the current
 * turn, read from {@code GameData.untappedLandsAtTurnStart} (snapshotted as each player's upkeep
 * begins, after the untap step). The target player's id comes from the stack entry's target
 * channel. The value is locked at turn start, so tapping lands in response to the trigger does
 * not change it (CR ruling for Power Surge).
 */
public record UntappedLandsAtTurnStart() implements DynamicAmount {
}
