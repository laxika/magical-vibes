package com.github.laxika.magicalvibes.model.effect;

/**
 * Rolls a d20 for each player, then prevents the source creature from attacking opponents who
 * tied for the highest result during the current combat.
 */
public record RollD20ForEachPlayerAndRestrictSourceAttacksEffect() implements CardEffect {
}
