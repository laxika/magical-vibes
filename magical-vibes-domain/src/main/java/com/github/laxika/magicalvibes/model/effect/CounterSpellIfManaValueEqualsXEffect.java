package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the target spell only if its mana value equals this spell's or ability's X. For an
 * ability, a {@link DiscardCardTypeCost} with {@code trackManaValue} snapshots X from the card
 * discarded to pay the cost (Hisoka, Minamo Sensei); for a spell it is the announced X
 * (Disrupting Shoal).
 */
public record CounterSpellIfManaValueEqualsXEffect() implements CounterSpellingEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
