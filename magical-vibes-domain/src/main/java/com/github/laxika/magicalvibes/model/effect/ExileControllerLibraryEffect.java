package com.github.laxika.magicalvibes.model.effect;


import java.util.Objects;

/**
 * The player {@code player} names exiles their whole library: the controller of the resolving ability by default
 * (Leveler, Thought Lash's unpaid-cumulative-upkeep trigger), or target player ({@link Subjects#targetPlayer()}, Jace,
 * the Mind Sculptor, a benign player target).
 *
 * @param player   whose library is exiled
 * @param faceDown whether the cards are exiled face down
 */
public record ExileControllerLibraryEffect(PlayerSubject player, boolean faceDown) implements CardEffect {

    public ExileControllerLibraryEffect {
        Objects.requireNonNull(player, "player");
        if (player instanceof PlayerSubject.Each) {
            throw new IllegalArgumentException("One player's library is exiled: " + player);
        }
    }

    public ExileControllerLibraryEffect() {
        this(false);
    }

    public ExileControllerLibraryEffect(boolean faceDown) {
        this(Subjects.you(), faceDown);
    }

    public ExileControllerLibraryEffect(PlayerSubject player) {
        this(player, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
