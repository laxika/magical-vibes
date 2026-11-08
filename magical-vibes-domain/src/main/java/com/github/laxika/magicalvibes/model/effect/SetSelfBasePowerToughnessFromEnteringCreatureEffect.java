package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the source permanent's base power and toughness until end of turn to the power and toughness of "that
 * creature": the stack entry's triggering permanent as the ability resolves, else as it last existed on the
 * battlefield (CR 608.2h). Eldrazi Mimic.
 */
public record SetSelfBasePowerToughnessFromEnteringCreatureEffect() implements CardEffect {
}
