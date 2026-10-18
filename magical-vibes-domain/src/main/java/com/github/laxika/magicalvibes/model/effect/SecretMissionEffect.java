package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.List;

/**
 * A Secret Mission conspiracy: before the game its owner secretly chooses one of {@code missions}.
 * During their end step, if the chosen mission's condition is met, they may reveal the choice and turn
 * the card face down as a special action; when they do, {@code reward} triggers (a reflexive ability).
 * Placed in {@code COMMAND_ZONE_STATIC}.
 */
public record SecretMissionEffect(List<Mission> missions, CardEffect reward) implements PregameChoiceEffect {

    public SecretMissionEffect {
        missions = List.copyOf(missions);
    }

    /** One mission: the text shown when choosing it and the condition that completes it. */
    public record Mission(String label, Condition condition) {
    }
}
