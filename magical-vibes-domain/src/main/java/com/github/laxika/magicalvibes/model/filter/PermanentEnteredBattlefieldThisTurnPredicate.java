package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents whose physical card entered the battlefield this turn. The check uses the permanent's
 * original card identity so a copy effect does not erase the fact that the permanent entered this
 * turn; it also accepts the current card identity for transformed permanents.
 *
 * @param underSourceControllersControl when true, only permanents that entered under the control of the
 *        ability's controller count ("entered the battlefield under your control this turn"), wherever
 *        they are now
 */
public record PermanentEnteredBattlefieldThisTurnPredicate(boolean underSourceControllersControl)
        implements PermanentPredicate {

    public PermanentEnteredBattlefieldThisTurnPredicate() {
        this(false);
    }
}
