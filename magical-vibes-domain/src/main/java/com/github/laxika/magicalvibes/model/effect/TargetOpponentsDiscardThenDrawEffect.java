package com.github.laxika.magicalvibes.model.effect;

/**
 * Each targeted opponent chooses a card in turn order, then all chosen cards are discarded; the
 * controller draws for each opponent whose discarded card has mana value below
 * {@code minimumManaValue}, including an opponent with no card to discard.
 */
public record TargetOpponentsDiscardThenDrawEffect(int minimumManaValue) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
