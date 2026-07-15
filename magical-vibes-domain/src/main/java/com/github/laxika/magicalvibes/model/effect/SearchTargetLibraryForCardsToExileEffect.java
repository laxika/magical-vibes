package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Searches target player's library for up to {@code count} cards and exiles them,
 * then that player shuffles. Targets a player. Used by Jester's Cap (fixed 3) and
 * Nightmare Incursion ("up to X" where X = number of Swamps you control).
 *
 * <p>{@code upTo} models the "up to" wording: when true the searcher may exile fewer
 * than {@code count} cards (drives {@code canFailToFind} in the search loop); when false
 * the search is mandatory up to the library size.
 */
public record SearchTargetLibraryForCardsToExileEffect(DynamicAmount count, boolean upTo) implements CardEffect {

    public SearchTargetLibraryForCardsToExileEffect(int count) {
        this(new Fixed(count), false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
