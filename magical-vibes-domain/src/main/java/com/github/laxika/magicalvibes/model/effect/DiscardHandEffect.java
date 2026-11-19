package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes one or more players discard their entire hand. The {@link PlayerSubject} names who
 * discards: {@code you()} (The Flame of Keld, "Discard your hand"), {@code eachPlayer()}
 * (Mindslicer, "each player discards their hand"), {@code targetPlayer()}, etc. Unlike {@link DiscardEffect} there is no
 * count or player choice — every card in the affected hand goes to the graveyard.
 *
 * @param player who discards their hand; only {@code targetPlayer()} declares a player target
 */
public record DiscardHandEffect(PlayerSubject player)
        implements CardEffect {

    /** Discards the controller's own hand. */
    public DiscardHandEffect() {
        this(Subjects.you());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
