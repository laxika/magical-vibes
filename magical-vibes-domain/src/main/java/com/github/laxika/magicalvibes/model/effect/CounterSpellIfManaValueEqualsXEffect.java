package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the target spell only if its mana value equals this ability's X — which a
 * {@link DiscardCardTypeCost} with {@code trackManaValue} snapshots from the card discarded to pay
 * the cost. Used by Hisoka, Minamo Sensei.
 */
public record CounterSpellIfManaValueEqualsXEffect() implements CounterSpellingEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}
