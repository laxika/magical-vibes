package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile this permanent, then return it to the battlefield transformed under its owner's control"
 * — resolved immediately (Jace, Vryn's Prodigy). The permanent that comes back is a new object, so
 * anything tracked on the old one (damage, counters, attachments, "this turn" markers) is gone.
 *
 * <p>Kytheon, Hero of Akros puts it on {@code END_OF_COMBAT_TRIGGERED} behind an intervening-if
 * condition for its "at end of combat" version.
 *
 * @param thenEffect optional "If you do, …" payload (Liliana, Heretical Healer's Zombie token),
 *                   resolved only when the exile-and-return actually happened — a source that has
 *                   already left the battlefield transforms nothing, so nothing else happens either
 * @param underControllerControl whether the returned permanent enters under its controller's
 *                               control instead of its owner's control
 */
public record ExileSelfAndReturnTransformedEffect(CardEffect thenEffect, boolean underControllerControl)
        implements CardEffect {

    /** Bare transform with no "if you do" rider (Jace, Vryn's Prodigy). */
    public ExileSelfAndReturnTransformedEffect() {
        this(null, false);
    }

    public ExileSelfAndReturnTransformedEffect(CardEffect thenEffect) {
        this(thenEffect, false);
    }

    /** Bare transform that returns the permanent under its current controller's control. */
    public ExileSelfAndReturnTransformedEffect(boolean underControllerControl) {
        this(null, underControllerControl);
    }
}
