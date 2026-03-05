package com.github.laxika.magicalvibes.model.effect;

/**
 * When this creature dies during combat, it deals {@code damage} to each creature
 * it blocked this combat (e.g. Cathedral Membrane).
 *
 * <p>This is an ON_DEATH marker effect. At death-trigger collection time the engine
 * resolves the blocked-attacker permanent IDs from the dying permanent's
 * {@code blockingTargetPermanentIds} and bakes them into the stack entry's
 * {@code targetPermanentIds}. Resolution then deals the specified damage to each
 * of those permanents.</p>
 */
public record DealDamageToBlockedAttackersOnDeathEffect(int damage) implements CardEffect {
}
