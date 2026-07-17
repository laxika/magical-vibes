package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next {@code amount} damage that would be dealt to this creature this turn is dealt to its owner
 * instead." No target: the protected creature is the ability's own source permanent and the redirected
 * damage goes to that permanent's controller (its owner). Applies to the next {@code amount} damage from
 * any source (combat or noncombat), then the shield is consumed. Used by Personal Incarnation.
 */
public record RedirectNextDamageToSelfToOwnerEffect(int amount) implements CardEffect {
}
