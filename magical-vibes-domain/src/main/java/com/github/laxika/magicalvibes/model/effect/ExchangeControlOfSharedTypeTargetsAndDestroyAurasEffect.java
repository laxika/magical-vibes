package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges control of the two permanents stored in {@code StackEntry.targetIds}: the first target
 * (an artifact, creature, or land the ability's controller controls) and the second target (a
 * permanent an opponent controls that shares one of those types with the first). If the exchange
 * happens, all Auras attached to either permanent are destroyed.
 *
 * <p>Used by Gauntlets of Chaos' {@code {5}, Sacrifice this artifact} ability. The cross-target
 * "shares one of those types" restriction is enforced at announcement via
 * {@code MultiTargetConstraint.SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE}; at resolution the handler
 * re-checks legality (CR 701.10) and only swaps controllers — then destroys the Auras — if both
 * targets are still legal.
 */
public record ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect() implements CardEffect {
}
