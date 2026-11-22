package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Choose an opponent at random": one of the players {@code players} names, picked at random once as the effect
 * resolves. A random pick is a step and no subject, so every later step reads the same player:
 * {@code Subjects.playersThisWay(StepKind.CHOOSE)} ("that player"). It records {@code StepKind.CHOOSE}.
 *
 * @param players among which players
 */
public record ChoosePlayerAtRandomEffect(PlayerSubject players) implements CardEffect {

    public ChoosePlayerAtRandomEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("A player chosen at random is no target: " + players);
        }
    }
}
