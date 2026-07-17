package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: players can't search libraries.
 *
 * <p>When {@code payableToIgnore} is {@code true} (Leonin Arbiter), any player may pay {2} to
 * ignore this effect until end of turn (handled at search resolution time). When {@code false}
 * (Mindlock Orb) the restriction is absolute — there is no way to pay to search.
 */
public record CantSearchLibrariesEffect(boolean payableToIgnore) implements CardEffect {

    /** Leonin Arbiter default: the restriction can be bypassed by paying {2}. */
    public CantSearchLibrariesEffect() {
        this(true);
    }
}
