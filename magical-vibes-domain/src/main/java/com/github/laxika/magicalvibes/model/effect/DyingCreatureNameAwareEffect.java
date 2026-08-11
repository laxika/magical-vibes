package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a death-trigger effect that needs the dying creature's name.
 *
 * <p>The trigger collector binds the name while the creature's last-known information is still
 * available. This is separate from {@link DyingCreatureCardAwareEffect}: a name-based effect can
 * still resolve when the dead card has left its graveyard before the trigger resolves.</p>
 */
public interface DyingCreatureNameAwareEffect {

    /** Returns a copy with the dying creature's name bound. */
    CardEffect boundToDyingCreatureName(String dyingCreatureName);
}
