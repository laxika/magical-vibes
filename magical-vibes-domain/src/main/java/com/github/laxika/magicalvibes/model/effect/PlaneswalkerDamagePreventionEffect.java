package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for static "if a source would deal damage to a planeswalker you control,
 * prevent N of that damage" effects (Djeru, With Eyes Open).
 *
 * <p>Read by {@code DamagePreventionService} as a FACT (the prevented {@code amount()}) rather than
 * dispatched by concrete type, so a second such card needs no service change.
 */
public interface PlaneswalkerDamagePreventionEffect extends CardEffect {

    /** Damage prevented from each source per damage event to a controlled planeswalker. */
    int amount();
}
