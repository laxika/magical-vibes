package com.github.laxika.magicalvibes.model.effect;

/**
 * Head Games: the target opponent puts their hand on top of their library, the caster searches that
 * library for that many cards, and the opponent puts them into their hand and shuffles.
 *
 * <p>{@code cardsToFind} is 0 for the card's own effect. When the opponent must first order the
 * cards they put on top (the caster can only search the top of the library), the resolution is
 * split and a follow-up effect carries the number of cards to find.
 */
public record HeadGamesEffect(int cardsToFind) implements CardEffect {

    public HeadGamesEffect() {
        this(0);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }
}
