package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile this permanent, then return it to the battlefield transformed under its owner's control"
 * — resolved immediately (Jace, Vryn's Prodigy). The permanent that comes back is a new object, so
 * anything tracked on the old one (damage, counters, attachments, "this turn" markers) is gone.
 * The step records RETURN (the permanent it became), so "If you do, …" after it is a
 * {@code ConditionalEffect(HappenedThisWay.returned(), …)} step (Liliana, Heretical Healer's Zombie token): a
 * source that has already left the battlefield transforms nothing.
 *
 * <p>Kytheon, Hero of Akros puts it on {@code Triggers.endOfCombat()} behind an intervening-if
 * condition for its "at end of combat" version.
 *
 * @param underControllerControl whether the returned permanent enters under its controller's
 *                               control instead of its owner's control
 */
public record ExileSelfAndReturnTransformedEffect(boolean underControllerControl) implements CardEffect {

    /** Bare transform under its owner's control (Jace, Vryn's Prodigy). */
    public ExileSelfAndReturnTransformedEffect() {
        this(false);
    }
}
