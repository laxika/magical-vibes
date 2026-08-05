package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player chooses {@code count} cards from their hand and puts them on top of their library
 * in any order (first chosen ends nearest the top). If their hand holds fewer than {@code count}
 * cards, they choose all of them.
 * <p>
 * Used by Stunted Growth ({@code count = 3}). Targets a player; harmful.
 */
public record TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
