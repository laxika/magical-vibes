package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the players {@code players} names can't search libraries: every player (Leonin Arbiter, Mindlock Orb)
 * or the controller's opponents ({@link Subjects#eachOpponent()}, Stranglehold).
 *
 * <p>When {@code payableToIgnore} is {@code true} (Leonin Arbiter), any player may pay {2} to
 * ignore this effect until end of turn (handled at search resolution time). When {@code false}
 * (Mindlock Orb) the restriction is absolute — there is no way to pay to search.
 *
 * <p>A resolution floats it on players with {@link GrantStaticEffectToPlayerEffect}: "players can't search libraries
 * this turn" (Shadow of Doubt) is {@code (you(), false)} granted to {@code eachPlayer()} until end of turn, "you" being
 * the player each copy is on. Read by {@code LibrarySearchRestrictions.find}.
 *
 * @param players         who can't search: {@code you()}, {@code eachPlayer()} or {@code eachOpponent()}
 * @param payableToIgnore whether a player may pay {2} to ignore this one until end of turn
 */
public record CantSearchLibrariesEffect(PlayerSubject players, boolean payableToIgnore) implements CardEffect {

    public CantSearchLibrariesEffect {
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    /** Leonin Arbiter default: the restriction can be bypassed by paying {2}. */
    public CantSearchLibrariesEffect() {
        this(true);
    }

    /** "Players can't search libraries", payable to ignore or not. */
    public CantSearchLibrariesEffect(boolean payableToIgnore) {
        this(Subjects.eachPlayer(), payableToIgnore);
    }
}
