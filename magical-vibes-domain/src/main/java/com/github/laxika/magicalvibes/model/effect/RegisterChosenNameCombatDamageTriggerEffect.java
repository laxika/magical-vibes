package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * "Whenever a creature with the chosen name deals combat damage to a player this turn, [effects]": a delayed triggered
 * ability (CR 603.7a) about the card name chosen earlier in the resolution.
 */
public record RegisterChosenNameCombatDamageTriggerEffect(List<CardEffect> effects) implements CardEffect {

    public RegisterChosenNameCombatDamageTriggerEffect {
        effects = List.copyOf(effects);
    }
}
