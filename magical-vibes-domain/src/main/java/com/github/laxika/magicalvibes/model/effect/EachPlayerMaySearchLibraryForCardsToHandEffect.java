package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * Each player, in APNAP order, may search their library for up to {@code count} cards, put them into
 * their hand, then shuffle. The search is a "may", so a player may take fewer than {@code count} (or
 * none). {@code creatureOnly} restricts the search to creature cards, which are also revealed
 * (Weird Harvest, count = the spell's X); an unrestricted search takes any card without revealing it
 * (Noble Benefactor, one card).
 */
public record EachPlayerMaySearchLibraryForCardsToHandEffect(DynamicAmount count,
                                                             boolean creatureOnly) implements CardEffect {

    /** Up to X creature cards per player, where X is the resolving spell's paid X. */
    public EachPlayerMaySearchLibraryForCardsToHandEffect() {
        this(new XValue(), true);
    }

    /** One card of any kind per player. */
    public static EachPlayerMaySearchLibraryForCardsToHandEffect oneCard() {
        return new EachPlayerMaySearchLibraryForCardsToHandEffect(new Fixed(1), false);
    }
}
