package com.github.laxika.magicalvibes.model.effect;

/**
 * Epic's upkeep trigger: "At the beginning of each of your upkeeps for the rest of the game, copy this spell
 * except for its epic ability. If the spell has any targets, you may choose new targets for the copy."
 * Put on the stack at the upkeep; the copy is created when it resolves. The stack entry's card is the spell
 * prototype the copy is made from.
 */
public record EpicUpkeepCopyEffect() implements CardEffect {
}
