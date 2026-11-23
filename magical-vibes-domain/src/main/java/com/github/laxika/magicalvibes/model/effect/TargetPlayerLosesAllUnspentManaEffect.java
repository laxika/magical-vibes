package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[Player] loses all unspent mana": each player {@code player} names empties their mana pool, losing the unspent mana
 * of every kind, the mana that wouldn't empty from it included. The target player of Mana Short and Power Sink's
 * "that player"; each player of Worldpurge ({@code Subjects.eachPlayer()}).
 *
 * @param player who loses the mana
 */
public record TargetPlayerLosesAllUnspentManaEffect(PlayerSubject player) implements CardEffect {

    public TargetPlayerLosesAllUnspentManaEffect {
        Objects.requireNonNull(player, "player");
    }

    /** The target player loses all unspent mana. */
    public TargetPlayerLosesAllUnspentManaEffect() {
        this(Subjects.targetPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
