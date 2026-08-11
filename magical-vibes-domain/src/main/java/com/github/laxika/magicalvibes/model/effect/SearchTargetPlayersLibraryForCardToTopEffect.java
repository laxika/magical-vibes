package com.github.laxika.magicalvibes.model.effect;

/**
 * Each target player searches their own library for a card, then shuffles and puts that card on
 * top of their library. The targets are processed one at a time through the shared library-search
 * interaction flow.
 */
public record SearchTargetPlayersLibraryForCardToTopEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
