package com.github.laxika.magicalvibes.service.outcome;

/**
 * Why a player would lose the game (CR 104.3). The reason selects which prevention effects apply
 * and is handed to every {@link LossReplacer}, since a replacement may cover only some reasons.
 *
 * <p>The constants are exactly the loss events a replacement effect can see. Two ways a player
 * can leave the game are deliberately absent:
 *
 * <ul>
 *   <li><b>Conceding</b> — CR 104.3a; a concession is not a replaceable event ("Lich's Mirror has
 *       no effect if you concede the game"). This engine has no concede path at all today.
 *   <li><b>An opponent winning</b> — a "you win the game" effect ends the game immediately rather
 *       than making anyone lose, so no loss ever occurs to replace ("Lich's Mirror has no effect
 *       if a spell or ability … states that a player 'wins the game.'"). Win-side handlers ask
 *       {@code GameOutcomeService.canPlayerWinGame} instead of raising a loss here.
 * </ul>
 */
public enum LossReason {

    /**
     * CR 704.5a — state-based action for having 0 or less life. The only reason a
     * {@code CantLoseGameFromLifeEffect} (Phyrexian Unlife) prevents.
     */
    LIFE,

    /** CR 704.5c — state-based action for having ten or more poison counters. */
    POISON,

    /**
     * CR 704.5b — state-based action for having attempted to draw from an empty library since
     * the last check.
     */
    EMPTY_LIBRARY,

    /**
     * CR 104.3n — an ability states that the player loses the game (Immortal Coil, Phage the
     * Untouchable, Triskaidekaphobia, Forbidden Crypt's failed draw).
     */
    EFFECT,
}
