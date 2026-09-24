package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles owned permanents into a library, then reveals that many cards and puts permanent cards
 * onto the battlefield in one or two type batches.
 */
public record MorphicTideEffect(boolean allPlayers, boolean auraCardsLast, boolean randomBottomOrder)
        implements CardEffect {

    /** Morphic Tide: all players, non-enchantment permanents first, bottom order chosen. */
    public MorphicTideEffect() {
        this(true, false, false);
    }

    /** Glimpse of Tomorrow: only the spell's controller, non-Aura permanents first, random bottom. */
    public static MorphicTideEffect glimpseOfTomorrow() {
        return new MorphicTideEffect(false, true, true);
    }
}
