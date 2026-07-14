package com.github.laxika.magicalvibes.model.effect;

/** How a {@link RemovalEffect} removes its single target from the battlefield. */
public enum RemovalKind {
    /** Destroys the target (subject to regeneration / indestructible). */
    DESTROY,
    /** Exiles the target. */
    EXILE,
    /** Returns the target to its owner's hand (bounce). */
    BOUNCE
}
