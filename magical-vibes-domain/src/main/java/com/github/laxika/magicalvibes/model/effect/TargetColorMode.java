package com.github.laxika.magicalvibes.model.effect;

/**
 * How the {@code colors} of a {@link TargetingRestrictionEffect} are interpreted.
 */
public enum TargetColorMode {

    /** Colors are ignored — the restriction applies to any source regardless of color. */
    ANY,

    /** A source is restricted if it is one of the listed colors (hexproof-from / can't-be-targeted-by). */
    BLOCKED_COLORS,

    /** A source is restricted unless it is one of the listed colors (Gaea's Revenge style). */
    ALLOWED_COLORS_ONLY
}
