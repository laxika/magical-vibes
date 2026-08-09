package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for up to {@code count} cards with the same name as the
 * target creature, revealing the cards and putting them into the controller's hand.
 */
public record SearchLibraryForCardsWithTargetCreatureNameEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
