package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the target spell only if its mana value equals this spell's or ability's X. For an
 * ability, a {@link DiscardCardTypeCost} with {@code trackManaValue} snapshots X from the card
 * discarded to pay the cost (Hisoka, Minamo Sensei); for a spell it is the announced X
 * (Disrupting Shoal).
 *
 * @param discardMustMatchTarget when true, the discard cost may only be paid with a card whose mana value
 *                               equals the target spell's ("Discard a card with mana value X: Counter target
 *                               spell with mana value X", Kozilek, the Great Distortion); otherwise any card
 *                               may be discarded and the counter simply fails on a mismatch (Hisoka)
 */
public record CounterSpellIfManaValueEqualsXEffect(boolean discardMustMatchTarget) implements CounterSpellingEffect {
    public CounterSpellIfManaValueEqualsXEffect() {
        this(false);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
