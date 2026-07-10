package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * Each player, in APNAP order, may search their library for up to {@code count} creature cards,
 * reveal them, put them into their hand, then shuffle. The search is a "may", so a player may
 * take fewer than {@code count} (or none). Used by Weird Harvest (count = the spell's X).
 */
public record EachPlayerMaySearchLibraryForCreaturesToHandEffect(DynamicAmount count) implements CardEffect {

    /** Up to X creature cards per player, where X is the resolving spell's paid X. */
    public EachPlayerMaySearchLibraryForCreaturesToHandEffect() {
        this(new XValue());
    }
}
