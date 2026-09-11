package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability for a granted effect that needs the permanent which granted it at resolution.
 *
 * <p>Card definitions use an unbound effect. Trigger collection creates a bound copy instead of
 * mutating the effect stored on the frozen card.</p>
 */
public interface GrantingPermanentAwareEffect extends CardEffect {

    /** Returns a copy bound to the permanent that granted this effect. */
    CardEffect withGrantingPermanentId(UUID permanentId);
}
