package com.github.laxika.magicalvibes.model.effect;

/**
 * The {@link PlayerSubject} mills half their library, rounded up or down: "target player mills half their library"
 * (Traumatize, Fleet Swallower), "defending player mills half their library" ({@code defendingPlayer()}, Terisian
 * Mindbreaker, Lord Xander, the Collector). Each player's half is counted from their own library as the effect
 * resolves.
 *
 * @param roundUp whether an odd library's half is rounded up
 * @param player  who mills; only {@code targetPlayer()} (the default) declares a (benign) player target
 */
public record MillHalfLibraryEffect(boolean roundUp, PlayerSubject player) implements CardEffect {

    /** Target player mills half their library. */
    public MillHalfLibraryEffect(boolean roundUp) {
        this(roundUp, Subjects.targetPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
