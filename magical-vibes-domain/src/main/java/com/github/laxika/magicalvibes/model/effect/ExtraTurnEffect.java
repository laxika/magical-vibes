package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** {@code player} takes {@code count} extra turns after this one: target player, or a player named without targeting. */
public record ExtraTurnEffect(int count, PlayerSubject player) implements CardEffect {

    public ExtraTurnEffect {
        Objects.requireNonNull(player, "player");
    }

    /** "Target player takes {@code count} extra turns after this one." */
    public ExtraTurnEffect(int count) {
        this(count, Subjects.targetPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
