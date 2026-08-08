package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell, then that spell's controller reveals their hand and discards each card with
 * the same name as a card spliced onto that spell (CR 702.47). Minamo's Meddling. With nothing
 * spliced onto the countered spell this is a plain counterspell.
 */
public record CounterSpellAndDiscardSplicedNamesEffect() implements CounterSpellingEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
