package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: while {@code condition} holds for the controller, damage that would reduce their
 * life total to less than {@code floor} reduces it to {@code floor} instead. This is a damage-only
 * replacement — it does not stop life loss, life payment, poison counters, or losing the game from
 * being at 0 or less life through other means, and the full damage is still dealt (lifelink and
 * damage triggers see the whole amount); only the life-total reduction is capped.
 *
 * <p>Worship is {@code (1, CONTROLS_A_CREATURE)}; Elderscale Wurm is
 * {@code (7, LIFE_AT_LEAST_FLOOR)}. Multiple such effects apply the highest active floor.
 */
public record DamageLifeFloorEffect(int floor, LifeFloorCondition condition) implements CardEffect {
}
