package com.github.laxika.magicalvibes.model.effect;


import java.util.Objects;

/**
 * The player {@code player} names becomes the monarch: the resolving ability's controller ({@link Subjects#you()}, the
 * default), target player ({@link Subjects#targetPlayer()}, a benign player target), or "that player" of the trigger
 * event ({@link Subjects#triggeringPlayer()}, Starscream, Seeker Leader).
 *
 * @param player who becomes the monarch
 */
public record BecomeMonarchEffect(PlayerSubject player) implements CardEffect {

    public BecomeMonarchEffect {
        Objects.requireNonNull(player, "player");
        if (player instanceof PlayerSubject.Each) {
            throw new IllegalArgumentException("One player becomes the monarch: " + player);
        }
    }

    /** "You become the monarch." */
    public BecomeMonarchEffect() {
        this(Subjects.you());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
