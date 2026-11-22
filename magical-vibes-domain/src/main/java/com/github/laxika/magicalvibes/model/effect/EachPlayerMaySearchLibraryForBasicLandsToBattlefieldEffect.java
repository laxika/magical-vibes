package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player may search their library for up to {@code count} basic land cards, put them onto the
 * battlefield, then shuffle. Players search in APNAP order (CR 101.4) and each search is optional
 * ("may"), so a player may take fewer than {@code count} cards, or none.
 *
 * <p>{@code players} narrows who searches, and the count is each searcher's own ("for each creature exiled this way,
 * its controller searches": {@code AmountThisWay(EXILE).controlledByYou()} over {@code playersThisWay(EXILE)}, Winds of
 * Abandon). The found lands enter together, then those players shuffle.
 *
 * <p>Used by New Frontiers, Veteran Explorer and Winds of Abandon.
 */
public record EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(DynamicAmount count,
                                                                           boolean enterTapped, boolean optionalSearch,
                                                                           PlayerSubject players)
        implements CardEffect {

    public EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect {
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who search are not targeted: " + players);
        }
    }

    public EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(DynamicAmount count, boolean enterTapped,
                                                                      boolean optionalSearch) {
        this(count, enterTapped, optionalSearch, Subjects.eachPlayer());
    }

    /** "Its controller searches": {@code players} must search, each for up to their own {@code count}. */
    public static EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect mandatoryTapped(
            DynamicAmount count, PlayerSubject players) {
        return new EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(count, true, false, players);
    }

    public EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(DynamicAmount count, boolean enterTapped) {
        this(count, enterTapped, true);
    }

    /** Requires the search, while still permitting failure to find cards with the required quality. */
    public static EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect mandatory(int count) {
        return new EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(new Fixed(count), false, false);
    }

    public EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(int count) {
        this(new Fixed(count), false);
    }

    public EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(DynamicAmount count) {
        this(count, false);
    }
}
