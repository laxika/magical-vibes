package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;

import java.util.Objects;

/**
 * The player {@code player} names chooses {@code count} cards from their hand and puts them on their library: on top
 * in any order (first chosen ends nearest the top), on the bottom, or on whichever of the two they pick for all of them
 * ({@code placement}). With fewer than {@code count} cards in hand they choose all of them. One player only: "each
 * player puts …" is {@code EachPlayerPutsCardFromHandOnTopOfLibraryEffect}.
 * "Target player puts three cards from their hand on top of their library" (Stunted Growth, {@code targetPlayer()}, a
 * harmful player target); "put two cards from your hand on top of your library in any order" after a draw step
 * (Brainstorm, Dream Cache, Enter the Infinite, {@code you()}).
 *
 * @param count     how many cards each player puts
 * @param placement where on their library the cards go
 * @param player    who puts cards from their hand
 */
public record TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(int count, HandToLibraryPlacement placement,
                                                                PlayerSubject player) implements CardEffect {

    public TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect {
        Objects.requireNonNull(placement, "placement");
        Objects.requireNonNull(player, "player");
        if (player instanceof PlayerSubject.Each) {
            throw new IllegalArgumentException("One player puts cards from their hand: " + player);
        }
    }

    /** Target player puts {@code count} cards from their hand on top of their library. */
    public TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(int count) {
        this(count, HandToLibraryPlacement.TOP, Subjects.targetPlayer());
    }

    /** "Put {@code count} cards from your hand on [top | the bottom | top or bottom] of your library." */
    public static TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect yours(int count,
                                                                         HandToLibraryPlacement placement) {
        return new TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(count, placement, Subjects.you());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
