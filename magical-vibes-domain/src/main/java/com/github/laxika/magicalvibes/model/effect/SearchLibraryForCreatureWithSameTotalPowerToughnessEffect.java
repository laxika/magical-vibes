package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a creature card whose power plus toughness equals the total power and
 * toughness of the stack entry's triggering permanent (Wild Pair), read as the ability resolves: its current power and
 * toughness, or as it last existed on the battlefield (CR 608.2h). Then puts that card onto the battlefield.
 */
public record SearchLibraryForCreatureWithSameTotalPowerToughnessEffect() implements CardEffect {
}
