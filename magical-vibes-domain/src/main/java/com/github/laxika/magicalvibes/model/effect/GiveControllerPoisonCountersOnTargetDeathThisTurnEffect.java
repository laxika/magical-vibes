package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger: when the targeted creature dies this turn, its controller
 * gets the specified number of poison counters.
 * <p>
 * At resolution, this effect reads the target permanent from the stack entry and records
 * the creature's card ID in {@code GameData.creatureGivingControllerPoisonOnDeathThisTurn}.
 * When that creature dies later in the same turn, the death handler checks this map and
 * applies the poison counters to the dying creature's controller.
 *
 * @param amount number of poison counters to give (typically 1)
 */
public record GiveControllerPoisonCountersOnTargetDeathThisTurnEffect(int amount) implements CardEffect {
}
