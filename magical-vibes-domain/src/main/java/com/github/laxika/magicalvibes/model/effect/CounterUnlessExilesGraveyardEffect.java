package com.github.laxika.magicalvibes.model.effect;

/** Counter target spell unless its controller exiles all cards from their graveyard. */
public record CounterUnlessExilesGraveyardEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
