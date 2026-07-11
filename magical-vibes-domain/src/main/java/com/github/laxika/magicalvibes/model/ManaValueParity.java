package com.github.laxika.magicalvibes.model;

/**
 * The parity (odd/even) of a permanent's mana value. Zero is even (CR / Ashling's Prerogative).
 * Chosen "as this permanent enters" and stored on the source {@link Permanent}.
 */
public enum ManaValueParity {
    ODD,
    EVEN;

    /** The parity of the given mana value. Zero is even. */
    public static ManaValueParity of(int manaValue) {
        return manaValue % 2 == 0 ? EVEN : ODD;
    }

    /** Whether the given mana value has this parity. */
    public boolean matches(int manaValue) {
        return of(manaValue) == this;
    }
}
