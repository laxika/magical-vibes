package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player may search their library for up to {@code count} basic land cards, put them onto the
 * battlefield, then shuffle. Players search in APNAP order (CR 101.4) and each search is optional
 * ("may"), so a player may take fewer than {@code count} cards, or none.
 *
 * <p>Used by New Frontiers, Natural Balance, and Veteran Explorer.
 */
public record EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(DynamicAmount count,
                                                                           boolean enterTapped, boolean optionalSearch)
        implements CardEffect {

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
