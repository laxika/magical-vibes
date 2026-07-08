package com.github.laxika.magicalvibes.model.effect;

/**
 * How long a {@link DoesntUntapEffect}'s untap-prevention lasts.
 */
public enum UntapLockCondition {
    /**
     * The permanent never untaps during its controller's untap step (a continuous static
     * effect on the permanent itself or its aura, e.g. Colossus of Sardia, Claustrophobia).
     */
    ALWAYS,
    /**
     * The target permanent doesn't untap for as long as the source permanent remains on the
     * battlefield (Dungeon Geists, Time of Ice). Tracked via
     * {@code Permanent.untapPreventedWhileSourceOnBattlefieldIds}.
     */
    WHILE_SOURCE_ON_BATTLEFIELD,
    /**
     * The target permanent doesn't untap for as long as the source permanent remains tapped
     * (Rust Tick). Tracked via {@code Permanent.untapPreventedByPermanentIds}.
     */
    WHILE_SOURCE_TAPPED
}
