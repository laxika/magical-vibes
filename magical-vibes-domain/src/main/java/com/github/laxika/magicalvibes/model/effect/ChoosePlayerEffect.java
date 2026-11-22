package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Choose an opponent", "choose a player": as the effect resolves its controller chooses one of the players named,
 * unasked when there is only one. A CHOOSE step: {@code Subjects.playersThisWay(StepKind.CHOOSE)} is that player.
 *
 * @param players who may be chosen; nobody here is targeted
 * @param label   what is chosen, for the prompt ("an opponent")
 */
public record ChoosePlayerEffect(PlayerSubject players, String label) implements CardEffect {

    public ChoosePlayerEffect {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(label, "label");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("A player chosen as the effect resolves is no target: " + players);
        }
    }

    /** "Choose an opponent". */
    public static ChoosePlayerEffect opponent() {
        return new ChoosePlayerEffect(Subjects.eachOpponent(), "an opponent");
    }
}
