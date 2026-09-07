package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Queues a forage rider after the forage action succeeds. */
public record ForageFollowUpEffect(CardEffect thenEffect) implements CardEffect {

    public ForageFollowUpEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
