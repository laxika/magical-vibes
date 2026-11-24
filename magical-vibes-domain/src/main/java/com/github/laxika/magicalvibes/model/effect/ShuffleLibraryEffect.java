package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Each player {@code players} names shuffles their library, in APNAP order.
 *
 * <p>{@code new ShuffleLibraryEffect(false)} is the controller's shuffle (Ponder's "you may shuffle");
 * {@code new ShuffleLibraryEffect(true)} targets a player who shuffles their library (Boggart Forager). Another subject
 * names the players without a target: "then each player who searched a library this way shuffles" (Tempt with
 * Discovery). Only the controller's and the targeted form may be wrapped in a "you may".
 *
 * @param players who shuffles; only {@code targetPlayer()} declares the (benign) player target
 */
public record ShuffleLibraryEffect(PlayerSubject players) implements CardEffect {

    public ShuffleLibraryEffect {
        Objects.requireNonNull(players, "players");
    }

    /** The controller's shuffle, or with {@code targetPlayer} the target player's. */
    public ShuffleLibraryEffect(boolean targetPlayer) {
        this(targetPlayer ? Subjects.targetPlayer() : Subjects.you());
    }

    /** Whether the shuffling player is the effect's own player target. */
    public boolean targetPlayer() {
        return players.declaresTarget();
    }

    @Override public TargetSpec targetSpec() {
        return targetPlayer() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
