package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of untapped lands the targeted player controlled at the beginning of the current
 * turn, read from {@code GameData.untappedLandsAtTurnStart} (snapshotted before the turn's untap
 * action). The target player's id comes from the stack entry's target
 * channel. The value is locked at turn start, so tapping lands in response to the trigger does
 * not change it.
 */
public record UntappedLandsAtTurnStart() implements DynamicAmount {
}
