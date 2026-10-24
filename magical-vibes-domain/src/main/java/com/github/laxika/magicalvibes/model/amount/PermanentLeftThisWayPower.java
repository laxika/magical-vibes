package com.github.laxika.magicalvibes.model.amount;

/**
 * "Its power" after "sacrifice/destroy …, then …": the total last-known power (CR 608.2h) of the
 * permanents an earlier step of the resolving entry recorded in its resolution scratchpad, each
 * counted as at least 0. 0 outside stack resolution.
 */
public record PermanentLeftThisWayPower() implements DynamicAmount {
}
