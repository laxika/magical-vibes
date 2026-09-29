package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.UUID;

/** Chooses a creature to tap for one opponent, then queues Nihiloor's reflexive control ability. */
public record NihiloorTapAndStealEffect(UUID opponentId) implements CardEffect {

    public NihiloorTapAndStealEffect {
        Objects.requireNonNull(opponentId, "opponentId");
    }
}
