package com.github.laxika.magicalvibes.model.effect;

/**
 * "It deals {@code damage} damage to each creature it blocked this combat" (Cathedral Membrane), the effect of a
 * "when this creature dies during combat" ability.
 *
 * <p>The creatures are the ones the stack entry's triggering permanent, the dying creature as it last existed on
 * the battlefield (CR 603.10a), had blocked this combat; they are read as the ability resolves. They aren't
 * targets. "During combat" is the ability's event condition ({@code DuringCombat}), not part of this effect.</p>
 */
public record DealDamageToBlockedAttackersOnDeathEffect(int damage) implements CardEffect {
}
