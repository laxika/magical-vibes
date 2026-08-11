package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell-cast trigger descriptor: put {@code amount} +1/+1 counters on the source permanent whenever
 * its controller casts a spell containing the source permanent's chosen color.
 */
public record PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect(int amount) implements CardEffect {
}
