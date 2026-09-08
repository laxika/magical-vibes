package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: until end of turn, damage that would reduce the controller's life total below
 * {@code floor} reduces it to {@code floor} instead.
 */
public record DamageLifeFloorUntilEndOfTurnEffect(int floor) implements CardEffect {
}
