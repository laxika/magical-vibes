package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell (or ability). The {@code destination} controls where the countered card is
 * placed instead of the graveyard — see {@link CounteredSpellDestination}. The no-arg constructor
 * defaults to {@link CounteredSpellDestination#GRAVEYARD} for the many plain "counter target spell"
 * cards.
 */
public record CounterSpellEffect(CounteredSpellDestination destination) implements CounterSpellingEffect {

    public CounterSpellEffect() {
        this(CounteredSpellDestination.GRAVEYARD);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}
