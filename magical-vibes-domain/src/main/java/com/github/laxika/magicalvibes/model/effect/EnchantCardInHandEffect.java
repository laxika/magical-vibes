package com.github.laxika.magicalvibes.model.effect;

/** Marks an Aura whose target is a card in its controller's hand. */
public record EnchantCardInHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.handCards(
                new com.github.laxika.magicalvibes.model.filter.CardTruePredicate()));
    }
}
